(ns freecoin-lib.util.populate-random-data
  (:require [clojure.test.check.generators :as gen]
            [freecoin-lib.db
             [account :as account]
             [freecoin :as db]]
            [freecoin-lib.core :as blockchain]
            [clj-storage.db.mongo :as mongo]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]
            [taoensso.timbre :as log]))

(defn string-generator [length]
  (gen/fmap #(apply str %) 
            (gen/vector gen/char-alpha length)))

(def account-generator (gen/hash-map :username (string-generator 8)
                                     :first-name (string-generator 8)
                                     :last-name (string-generator 8)
                                     :email (string-generator 8)
                                     :password (string-generator 8)))

(defn- choose-random-account [account-store n]
  (nth (account/fetch-all account-store)
       (rand-int n)))

(defn transaction-generation [bk stores n]
  (blockchain/create-transaction bk
                                 (-> stores
                                     :account-store 
                                     (choose-random-account n)
                                     :email)
                                 (rand)
                                 (-> stores
                                     :account-store
                                     (choose-random-account n)
                                     :email)
                                 {}))

(defn populate-data [n]
  (let [uri "mongodb://localhost:27017/freecoin"
        db (mongo/get-mongo-db uri)
        stores (db/create-freecoin-stores db)
        bk (blockchain/->Mongo stores)
        gen-account
        (gen/fmap
         #(account/new-account! (:account-store stores) (merge % {:activated true
                                                                  :flags [:admin]})) 
         account-generator)]
    (dotimes [n n] (transaction-generation bk stores n))
    #_(gen/sample gen-account n)
    ))
