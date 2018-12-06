(ns freecoin-lib.db.freecoin
  (:require [clj-storage.db.mongo :as mongo]
            [clj-storage.core :as storage]
            [taoensso.timbre :as log]
            ))

(defn stores-params-m [args]
  {"wallet-store" {}
   "confirmation-store" {}
   "transaction-store"  {}
   "tag-store" {}
   "apikey-store" {:index :apikey}})

(defn create-freecoin-stores [db & args]
  (log/debug "Creating the freecoin mongo stores")
  (mongo/create-mongo-stores
   db
   (stores-params-m args)))

(defn create-in-memory-stores []
  (log/debug "Creating in memory stores for testing the freecoin")
  (storage/create-in-memory-stores (keys (stores-params-m []))))
