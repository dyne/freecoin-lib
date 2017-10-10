(ns freecoin-lib.db.freecoin
  (:require [clj-storage.db.mongo :as mongo]))

(defn create-freecoin-stores [db]
  (let [colls {"wallet-store" {}
               "confirmation-store" {}
               "transaction-store"  {}
               "account-store" {}
               "tag-store" {}
               "password-recovery-store" {:expireAfterSeconds 1800}}]
    (mongo/create-mongo-stores
     db
     (keys colls)
     (vals colls))))
