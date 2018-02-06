(ns freecoin-lib.test.core
  (:require [midje.sweet :refer :all]
            [freecoin-lib
             [core  :as blockchain]
             [schemas :as fc]]
            [freecoin-lib.db.freecoin :as db]
            [clj-storage.db.mongo :as mongo]
            [clj-storage.core :as storage]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(facts "Validate agaist schemas"

         (fact Created Mongo stores fit the schema
               (let [uri "mongodb://localhost:27017/some-db"
                     db (mongo/get-mongo-db uri)
                     stores-m (db/create-freecoin-stores  db)]

                 (s/validate fc/StoresMap stores-m) => truthy

                 ;; TODO validate TTl
                 (blockchain/new-mongo stores-m) => truthy

                 (blockchain/new-mongo nil) => (throws Exception)))

         (fact Created BTC RPC record validates against the schemas
               (let [conf-file (-> "sample-btc-rpc.conf"
                                   (clojure.java.io/resource)
                                   (.getPath))]
                 (blockchain/new-btc-rpc "FAIR" conf-file) => truthy

                 (blockchain/new-btc-rpc nil) => (throws Exception))))
