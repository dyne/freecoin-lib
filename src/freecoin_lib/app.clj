(ns freecoin-lib.app
  (:require [taoensso.timbre :as log]
            [freecoin-lib.core :refer [new-mongo]]
            [freecoin-lib.schemas :refer [Config]]
            [freecoin-lib.config :as config]
            [freecoin-lib.db.mongo :as mongo]
            [freecoin-lib.db.storage :as storage]
            [schema.core :as s]))


;; launching and halting the app
(defonce ^:private app-state (atom {}))

(defn- mongo-conf-to-uri [conf]
  (if-let [m conf]
    (str "mongodb://" (:host m) ":" (:port m) "/" (:db m))))

(defn connect-mongo
  "Connect a mongo database for metadata (required)" [conf]
  (if (s/validate Config conf)
    (-> (:mongo conf)
        mongo-conf-to-uri
        mongo/get-mongo-db
        storage/create-mongo-stores)))
;; TODO: else return error

(defn start [config]
  (if-let [conf config]
    (if (s/validate Config conf)
      (let [db         (-> conf mongo-conf-to-uri config/mongo-uri mongo/get-mongo-db)
            stores     (storage/create-mongo-stores db) ;; here ttl optional arg
            backend    (new-mongo stores)]
        ;; return the base context
        {:db db
         :config (:freecoin config)
         :backend backend}))))
;; TODO: handle errors consistently

(defn disconnect-mongo [ctx]
  (if-let [db (:wallet-store ctx)] (mongo/disconnect db)))
