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

(ns freecoin-lib.confirmation
  (:require [midje.sweet :refer :all]
            [freecoin-lib.db
             [confirmation :as confirmation]]
            [clj-storage.core :as storage]
            [freecoin-lib.test-helpers.store :as test-store]))

(def sender-email "sender@mail.com")
(def recipient-email "recipient@mail.com")

(facts "Can create and fetch a transaction confirmation"
       (let [uuid-generator (constantly "a-uuid")
             confirmation-store (storage/create-memory-store)]
         (fact "can create a transaction confirmation"
               (let [confirmation (confirmation/new-transaction-confirmation! confirmation-store uuid-generator
                                                                              sender-email recipient-email 10M)]
                 confirmation => (just {:uid "a-uuid"
                                        :type :transaction
                                        :data {:sender-email sender-email
                                               :recipient-email recipient-email
                                               :amount 10M
                                               :tags #{}}})))

         (fact "can fetch a transaction confirmation by its uid"
               (confirmation/fetch confirmation-store "a-uuid")
               => (just {:uid "a-uuid"
                         :type :transaction
                         :data {:sender-email sender-email
                                :recipient-email recipient-email
                                :amount 10M
                                :tags #{}}}))

         (fact "transaction confirmations can have tags"
               (let [confirmation (confirmation/new-transaction-confirmation! confirmation-store
                                                                              uuid-generator
                                                                              sender-email
                                                                              recipient-email
                                                                              10M
                                                                              #{:air-drop})]
                 confirmation => (just {:uid "a-uuid"
                                        :type :transaction
                                        :data {:sender-email sender-email
                                               :recipient-email recipient-email
                                               :amount 10M
                                               :tags #{:air-drop}}})))))

(fact "Can delete a confirmation"
      (let [confirmation-store (storage/create-memory-store)
            confirmation (confirmation/new-transaction-confirmation! confirmation-store (constantly "uid")
                                                                     sender-email recipient-email 10M)]
        (test-store/summary confirmation-store) => (contains {:entry-count 1})
        (confirmation/delete! confirmation-store "uid")
        (test-store/summary confirmation-store) => (contains {:entry-count 0})))
