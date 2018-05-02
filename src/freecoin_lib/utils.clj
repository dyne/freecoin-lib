;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015-2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns freecoin-lib.utils
  (:require [failjure.core :as f]
            [taoensso.timbre :as log])
  (:import  org.bson.types.ObjectId
            [org.bson.types Decimal128]))

(declare log!)

(defn trunc
  "Truncate string at length"
  [s n]
  {:pre [(> n 0)
         (seq s)]} ;; not empty
  (subs s 0 (min (count s) n)))

(defn compress
  "Compress a collection removing empty elements"
  [coll]
  (clojure.walk/postwalk #(if (coll? %) (into (empty %) (remove nil? %)) %) coll))

(defmacro bench
  "Times the execution of forms, discarding their output and returning
  a long in nanoseconds."
  ([& forms]
   `(let [start# (System/nanoTime)]
      ~@forms
      (- (System/nanoTime) start#))))

;; For working with maps
(defn select-all-or-nothing [m keys]
  (when (every? (partial contains? m) keys)
    (select-keys m keys)))

;; LOGS are all in this format: name trace description
(defn dolog [name trace desc]
  (println (format "LOG: %s (%s) %s" name trace desc)))

;; tweak here to activate logs
(defn log! [n t d]
  (condp = n
    'ACK   (dolog n t d)
    'FACT  nil ;; (dolog n t d)
    (dolog n t d)))

;; The reason for those two functions is that Mongo is using fixed decimals and there might be loss of presicion https://stackoverflow.com/questions/27967460/lift-store-bigdecimal-in-mongodb
;; UPDATE: Not the case anymore: https://jira.mongodb.org/browse/SERVER-1393
(defn ^:deprecated bigdecimal->long
  "Convert from BigDecimal to long for storage into mongo"
  [bd]
  (.longValue (* bd 100000)))

(defn ^:deprecated long->bigdecimal
  "Convert from long to BigDecimal for retrievals from mongo"
  [l]
  (/ (BigDecimal. l) 100000))

;;--------------- Validations for input amounts

(defn- string-input? [amount]
  (f/if-let-ok? (string? amount)
    amount
    (f/fail "The input should be a string.")))

(defn- parsed-dec128? [amount]
  (f/if-let-ok? [parsed-amount (f/try* (Decimal128/parse amount))]
    parsed-amount
    (f/fail "The amount is not valid.")))

(defn- decimal128? [amount]
  (if (instance? org.bson.types.Decimal128 amount)
    amount
    (f/fail "Amount is not of the right type.")))

(defn- positive-value? [amount]
  (if (.isNegative amount)
    (f/fail "Negative values not allowed.")
    amount))

(defn validate-input-amount [amount]
  (f/attempt-all [string-amount (string-input? amount)
                  dec128-amount (parsed-dec128? string-amount)
                  positive-amount (positive-value? dec128-amount)]
                 positive-amount
                 (f/when-failed [e]
                   (log/warn "The amount is not a valid number." amount)
                   (f/fail (f/message e)))))

