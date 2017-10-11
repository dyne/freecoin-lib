(ns freecoin-lib.db.freecoin
  (:require [clj-storage.db.mongo :as mongo]
            [clj-storage.core :as storage]
            [taoensso.timbre :as log]))

(defn stores-params-m [args]
  {"wallet-store" {}
   "confirmation-store" {}
   "transaction-store"  {}
   "account-store" {}
   "tag-store" {} 
   "password-recovery-store" {:expireAfterSeconds (if-let [arg-map (first args)]
                                                    (:ttl-password-recovery arg-map)
                                                    1800)}})

(defn create-freecoin-stores [db & args]
  (mongo/create-mongo-stores
   db
   (stores-params-m args)))

(defn create-in-memory-stores []
  (storage/create-in-memory-stores (keys (stores-params-m []))))
