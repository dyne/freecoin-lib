;; Freecoin-lib - library to facilitate blockchain functions

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>
;; Aspasia Beneti <aspra@dyne.org>

;; With contributions by
;; Carlo Sciolla
;; Arjan Scherpenisse <arjan@scherpenisse.net>

;; Freecoin-lib is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; Freecoin-lib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify Freecoin-lib, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns freecoin-lib.config
  (:require [environ.core :as env]))

(def env-vars #{:port :host :base-url :secure :debug
                :client-id :client-secret :rpc-config
                :sawtooth-api :petition-api})

(defn create-config []
  (select-keys env/env env-vars))

(defn get-env
  "Like a normal 'get' except it also ensures the key is in the env-vars set"
  ([config-m key]
   (get config-m (env-vars key)))
  ([config-m key default]
   (get config-m (env-vars key) default)))

(defn port [config-m]
  (Integer. (get-env config-m :port "8000")))

(defn host [config-m]
  (get-env config-m :host "localhost"))

(defn base-url [config-m]
  (get-env config-m :base-url "http://localhost:8000"))

(defn client-id [config-m]
  (get-env config-m :client-id))

(defn client-secret [config-m]
  (get-env config-m :client-secret))

(defn rpc-config [config-m]
  (get-env config-m :rpc-config))

(defn cookie-secret [config-m]
  (get-env config-m :cookie-secret "encryptthecookie"))

(defn debug [config-m]
  (get-env config-m :debug true))

(defn- get-docker-mongo-uri [config-m]
  (when-let [mongo-ip (get-env config-m :mongo-port-27017-tcp-addr)]
    (format "mongodb://%s:27017/freecoin" mongo-ip)))

(defn mongo-uri [config-m]
  (or (get-docker-mongo-uri config-m)
      (get-env config-m :mongo-uri)
      "mongodb://localhost:27017/freecoin"))

(defn secure? [config-m]
  (not (= "false" (get-env config-m :secure "true"))))
