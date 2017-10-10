;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti  <aspra@dyne.org>

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

(ns freecoin-lib.db.account
  (:require [clj-storage.core :as storage]
            [buddy.hashers :as hashers]))

(defn- generate-hash [password]
  (hashers/derive password {:alg :pbkdf2+sha512}))

(defn new-account!
  [account-store {:keys [first-name last-name email password flags] :as account-map}]
  (storage/store! account-store :email (-> account-map
                                         (assoc :activated false)
                                         (assoc :flags (or flags []))
                                         (update :password #(generate-hash %)))))

(defn activate! [account-store email]
  (storage/update! account-store email #(assoc % :activated true)))

(defn fetch [account-store email]
  (some-> (storage/fetch account-store email)
          (update :flags (fn [flags] (map #(keyword %) flags)))))

(defn fetch-by-activation-id [account-store activation-id]
  (first (storage/query account-store {:activation-id activation-id})))

(defn update-activation-id! [account-store email activation-link]
  (storage/update! account-store email #(assoc % :activation-id activation-link)))

(defn delete! [account-store email]
  (storage/delete! account-store email))

(defn correct-password? [account-store email candidate-password]
  (hashers/check candidate-password
   (:password (fetch account-store email))))

(defn update-password! [account-store email password]
  (storage/update! account-store email #(assoc % :password (generate-hash password))))

(defn add-flag! [account-store email flag]
  (storage/update! account-store email (fn [account] (update account :flags #(conj % flag)))))

(defn remove-flag! [account-store email flag]
  (storage/update! account-store email (fn [account] (update account :flags #(remove #{flag} %)))))
