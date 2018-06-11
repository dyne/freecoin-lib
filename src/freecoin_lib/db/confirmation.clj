;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>

;; With contributions by
;; Duncan Mortimer <dmortime@thoughtworks.com>

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

(ns freecoin-lib.db.confirmation
  (:require [clj-storage.core :as storage]
            [freecoin-lib.utils :as util]))

(defn new-transaction-confirmation!
  ([confirmation-store uuid-generator sender-email recipient-email amount]
   (new-transaction-confirmation! confirmation-store uuid-generator sender-email recipient-email amount #{}))
  ([confirmation-store uuid-generator sender-email recipient-email amount tags]
   (let [confirmation {:uid (uuid-generator)
                       :type :transaction
                       :data {:sender-email sender-email
                              :recipient-email recipient-email
                              :amount amount
                              :tags tags}}
         stored (some-> (storage/store! confirmation-store :uid confirmation))]
     stored)))

(defn fetch [confirmation-store email]
  (some-> (storage/fetch confirmation-store email)))

(defn delete! [confirmation-store email]
  (storage/delete! confirmation-store email))
