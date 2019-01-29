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

(ns freecoin-lib.db.wallet
  (:require [freecoin-lib.core :as blockchain]
            [clj-storage.core :as storage]))

(defn- empty-wallet [name email]
  {:name  name        ;; identifier, case insensitive, space counts
   :email email       ;; verified email account
;   :info nil          ;; misc information text on the account
;   :creation-date nil ;; date on which the wallet was created
;   :last-login nil    ;; last time this participant logged in succesfully
;   :last-login-ip nil ;; connection ip address of the last succesful login
;   :failed-logins nil ;; how many consecutive failed logins were attempted
   :public-key nil    ;; public asymmetric key for off-the-blockchain encryption
   :private-key nil   ;; private asymmetric key for off-the-blockchain encryption
   :account-id nil    ;; blockchain account id
   })

(defn secret->apikey [secret]
  (str (:cookie secret) "::" (:_id secret)))

(defn secret->participant-shares [secret]
  (take 4 (:slices secret)))

(defn secret->organization-shares [secret]
  (->> (:slices secret)
       (drop 3) (take 4)))

(defn secret->auditor-shares [secret]
  (take-last 3 (:slices secret)))

(defn new-empty-wallet! [wallet-store blockchain name email]
  (let [{:keys [account-id account-secret]} (blockchain/create-account blockchain name)
        wallet (-> (empty-wallet name email)
                   (assoc :account-id account-id))]
    {:wallet       (storage/store! wallet-store :email wallet)
     :apikey       (secret->apikey              account-secret)
     :participant  (secret->participant-shares  account-secret)
     :organization (secret->organization-shares account-secret)
     :auditor      (secret->auditor-shares      account-secret)}))

(defn fetch [wallet-store email]
  (storage/fetch wallet-store email))

(defn fetch-by-name [wallet-store name]
  (first (storage/query wallet-store {:name name})))

(defn fetch-by-account-id [wallet-store name]
  (first (storage/query wallet-store {:account-id name})))

(defn query
  ([wallet-store] (query wallet-store {}))
  ([wallet-store query-m] (storage/query wallet-store query-m)))
