;; Freecoin-lib - library to facilitate blockchain functions

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2017- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; Freecoin-lib is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; Freecoin-lib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify Freecoin-lib, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns freecoin-lib.test.db.transaction
  (:require [midje.sweet :refer [against-background before after facts fact =>]] 
            [clj-storage.core :as storage]
            [clj-storage.test.db.test-db :as test-db]
            [freecoin-lib
             [core :as blockchain]]
            [freecoin-lib.db.freecoin :as freecoin]
            [taoensso.timbre :as log]
            [clj-time.core :as t]))

(against-background [(before :contents (test-db/setup-db))
                     (after :contents (test-db/teardown-db))]

                    (facts "Create some transactions"
                           (let [stores-m (freecoin/create-freecoin-stores (test-db/get-test-db))
                                 transaction-store (:transaction-store stores-m)]
                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "B"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 1
                                                                     :timestamp (t/date-time 2017 12 1)})

                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 2
                                                                     :timestamp (t/date-time 2017 12 1)})

                             (storage/store! transaction-store :id_ {:from-id "B"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 2
                                                                     :timestamp (t/date-time 2016 12 1)})
                             (storage/store! transaction-store :id_ {:from-id "C"
                                                                     :to-id "A"
                                                                     :currency "FAIR"
                                                                     ;; TODO add tags and test them
                                                                     :amount 20
                                                                     :timestamp (t/date-time 2016 12 1)})
                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                             
                                                                     :amount 50
                                                                     :description "something"
                                                                     :timestamp (t/date-time 2015 12 1)})

                             (let [mongo-bc (blockchain/new-mongo "Testcoin" stores-m)]
                               (fact "The budget per account is correct"
                                     (blockchain/get-balance mongo-bc "A") => -33M
                                     (blockchain/get-balance mongo-bc "B") => -1M
                                     (blockchain/get-balance mongo-bc "C") => 34M)
                               (fact "Retrieving transactions with and without paging works"
                                     (count (blockchain/list-transactions mongo-bc {})) => 5
                                     (count (blockchain/list-transactions mongo-bc {:currency "mongo" :account-id "A"})) => 3
                                     ;; count doesnt do anything for Mongo
                                     (count (blockchain/list-transactions mongo-bc {:count 1})) => 5

                                     ;; Page 0 and 1 return the same thing i.e. the first page
                                     (count (blockchain/list-transactions mongo-bc {:page 0 :per-page 2})) => 2
                                     (count (blockchain/list-transactions mongo-bc {:page 1 :per-page 2})) => 2
                                     (count (blockchain/list-transactions mongo-bc {:page 2 :per-page 2})) => 2
                                     (count (blockchain/list-transactions mongo-bc {:page 3 :per-page 2})) => 1
                                     (count (blockchain/list-transactions mongo-bc {:page 4 :per-page 2})) => 0
                                     ;; Passing the paging limit throws an error
                                     (:message (blockchain/list-transactions mongo-bc {:page 0 :per-page 200})) => "Cannot request more than 100 transactions."
                                     ;; Defualts to 10 per-page
                                     (count (blockchain/list-transactions mongo-bc {:page 1})) => 5
                                     (count (blockchain/list-transactions mongo-bc {:page 2})) => 0
                                     ;; Page 0 and 1 return the same thing i.e. the first page
                                     (let [first-two-entries '({:amount 2 :currency "mongo" :from-id "A" :to-id "C"}
                                                               {:amount 1 :currency "mongo" :from-id "A" :to-id "B"})
                                           second-two-entries '({:amount 20 :currency "FAIR" :from-id "C" :to-id "A"}
                                                                {:amount 2 :currency "mongo" :from-id "B" :to-id "C"})
                                           last-entry '({:amount 50 :currency "mongo" :from-id "A" :to-id "C" :description "something"})]
                                       (reverse (map #(dissoc % :timestamp)
                                                     (blockchain/list-transactions mongo-bc {:page 0 :per-page 2}))) => first-two-entries
                                       (reverse (map #(dissoc % :timestamp)
                                                     (blockchain/list-transactions mongo-bc {:page 1 :per-page 2}))) => first-two-entries
                                       (reverse (map #(dissoc % :timestamp)
                                                     (blockchain/list-transactions mongo-bc {:page 2 :per-page 2}))) => second-two-entries 
                                       (reverse (map #(dissoc % :timestamp)
                                                     (blockchain/list-transactions mongo-bc {:page 3 :per-page 2}))) => last-entry)
                                     (fact "Paging works also with other criteria"
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A"})) => 4
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A" :page 1 :per-page 2})) => 2
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A" :page 3 :per-page 2})) => 0)
                                     (fact "The count of transactions works properly."
                                           (blockchain/count-transactions mongo-bc {}) => (count (storage/query transaction-store {}))
                                           (blockchain/count-transactions mongo-bc {:currency "mongo"}) => (count (storage/query transaction-store {:currency "mongo"})))

                                     (fact "Filtering by dates works properly."
                                           (count (blockchain/list-transactions mongo-bc {})) => 5
                                           (count (blockchain/list-transactions mongo-bc {:from (t/date-time 2016 11 30)})) => 4
                                           (count (blockchain/list-transactions mongo-bc {:from (t/date-time 2016 11 30)
                                                                                          :to (t/date-time 2016 12 2)})) => 2)
                                     (fact "Filtering by desciption also works."
                                           (count (blockchain/list-transactions mongo-bc {:description "something"})) => 1
                                           (count (blockchain/list-transactions mongo-bc {:description "something else"})) => 0))))))
