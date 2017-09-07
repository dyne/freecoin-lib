(ns freecoin-lib.test.core
  (:require [midje.sweet :refer :all]
            [freecoin-lib
             [core  :as blockchain]
             [freecoin-schema :as fc]]
            [freecoin-lib.db
             [storage :as storage]
             [mongo :as mongo]]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(facts "Validate agaist schemas"

         (fact "Created stores fit the schema"
               (let [uri "mongodb://localhost:27017/some-db"
                     db (mongo/get-mongo-db uri)
                     stores-m (storage/create-mongo-stores db)]

                 (s/validate fc/StoresMap stores-m) => truthy

                 (blockchain/new-mongo stores-m) => truthy

                 (blockchain/new-mongo nil) => (throws Exception))))
