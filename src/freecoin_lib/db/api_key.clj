;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2018- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti  <aspra@dyne.org>

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <http://www.gnu.org/licenses/>.

(ns freecoin-lib.db.api-key
  (:require [clj-storage.core :as storage]
            [taoensso.timbre :as log]))

(defn create-apikey! [{:keys [apikey-store client-app api-key] :as apikey-map}] 
  (storage/store! apikey-store :client-app (dissoc apikey-map
                                                   :apikey-store)))

(defn fetch-by-client-app [apikey-store-store client-app]
  (storage/fetch apikey-store-store client-app))

(defn fetch-by-api-key [apikey-store api-key]
  (first (storage/query apikey-store {:api-key api-key})))

(defn fetch-all [apikey-store]
  (storage/list-per-page apikey-store {} 0 0))
