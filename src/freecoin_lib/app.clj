(ns freecoin-lib.app
  (:require [taoensso.timbre :as log]
            [freecoin-lib.core :refer [new-mongo]]
            [freecoin-lib.config :as config]
            [freecoin-lib.db.mongo :as mongo]
            [freecoin-lib.db.storage :as storage]))


;; launching and halting the app
(defonce ^:private app-state (atom {}))

(defn connect-db [config]
  (-> config config/mongo-uri mongo/get-mongo-db))

(defn disconnect-db [ctx]
  (if-let [db (:db ctx)]
      (mongo/disconnect db)))

(defn start [ctx]
  (if (contains? ctx :backend) ctx
    (let [config     (config/create-config)
          ;; TODO: use config as argument, not single config keys
          db         (-> config config/mongo-uri mongo/get-mongo-db)
          stores     (storage/create-mongo-stores db) ;; here ttl optional arg
          backend    (new-mongo stores)]
      (assoc ctx
             :db db
             :config config
             :backend backend))))

(defn stop [ctx]
  (if-let [db (:db ctx)] (mongo/disconnect db)))

;; For running from the repl
;; (defn start []
;;   (swap! app-state (comp launch connect-db)))

;; (defn stop []
;;   (swap! app-state (comp disconnect-db halt)))
