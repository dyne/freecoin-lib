(ns freecoin-lib.util.populate-random-data
  (:require [clojure.test.check.generators :as gen]
            [freecoin-lib.db
             [account :as account]
             [freecoin :as db]]
            [clj-storage.db.mongo :as mongo]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]
            [taoensso.timbre :as log]))

(def stores (atom {}))

(defn string-generator [length]
  (gen/fmap #(apply str %) 
            (gen/vector gen/char-alpha length)))

(def account-generator (gen/hash-map :username (string-generator 8)
                                     :first-name (string-generator 8)
                                     :last-name (string-generator 8)
                                     :email (string-generator 8)
                                     :password (string-generator 8)))

(defn populate-data [n]
  (let [uri "mongodb://localhost:27017/freecoin"
        db (mongo/get-mongo-db uri)
        _ (reset! stores (db/create-freecoin-stores db))
        gen-account
        (gen/fmap
         #(account/new-account! (:account-store @stores) (merge % {:activated true
                                                                   :flags [:admin]})) 
         account-generator)]
    (gen/sample gen-account n)))





(comment
  (require '[clj-storage.core :as storage])
  (storage/empty-db-stores! (log/spy @stores))
  )
