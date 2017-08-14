;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; With contributions by
;; Duncan Mortimer <dmortime@thoughtworks.com>

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

(ns freecoin-lib.test.db.test-db
  (:require [monger.core :as monger]))

(def test-db-name "test-db")
(def test-db-uri (format "mongodb://localhost:27017/%s" test-db-name))

(def db-and-conn (atom nil))

(defn get-test-db []
  (:db @db-and-conn))

(defn get-test-db-connection []
  (:conn @db-and-conn))

(defn- drop-db [db-and-conn]
  (monger/drop-db (:conn db-and-conn) test-db-name)
  db-and-conn)

(defn setup-db []
  (->> (monger/connect-via-uri test-db-uri)
       drop-db
       (reset! db-and-conn)))

(defn teardown-db []
  (drop-db @db-and-conn)
  (monger/disconnect (get-test-db-connection))
  (reset! db-and-conn nil))
