;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

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

(ns freecoin-lib.test.db.transaction
  (:require [midje.sweet :refer :all] 
            [clj-storage.db.mongo :as mongo]
            [clj-storage.core :as storage]
            [clj-storage.test.db.test-db :as test-db]
            [freecoin-lib
             [utils :as utils]
             [core :as blockchain]]
            [freecoin-lib.db.freecoin :as freecoin]
            [taoensso.timbre :as log]))

(against-background [(before :contents (test-db/setup-db))
                     (after :contents (test-db/teardown-db))]

                    (facts "Create some transactions"
                           (let [stores-m (freecoin/create-freecoin-stores (test-db/get-test-db))
                                 transaction-store (:transaction-store stores-m)]
                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "B"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 1})

                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 2})

                             (storage/store! transaction-store :id_ {:from-id "B"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                                                     ;; TODO add tags and test them
                                                                     :amount 2})
                             (storage/store! transaction-store :id_ {:from-id "C"
                                                                     :to-id "A"
                                                                     :currency "FAIR"
                                                                     ;; TODO add tags and test them
                                                                     :amount 20})
                             (storage/store! transaction-store :id_ {:from-id "A"
                                                                     :to-id "C"
                                                                     :currency "mongo"
                                             
                                                                     :amount 50})

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
                                           last-entry '({:amount 50 :currency "mongo" :from-id "A" :to-id "C"})]
                                       (blockchain/list-transactions mongo-bc {:page 0 :per-page 2}) => first-two-entries
                                       (blockchain/list-transactions mongo-bc {:page 1 :per-page 2}) => first-two-entries
                                       (blockchain/list-transactions mongo-bc {:page 2 :per-page 2}) => second-two-entries 
                                       (blockchain/list-transactions mongo-bc {:page 3 :per-page 2}) => last-entry)
                                     (fact "Paging works also with other criteria"
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A"})) => 4
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A" :page 1 :per-page 2})) => 2
                                           (count (blockchain/list-transactions mongo-bc { :account-id "A" :page 3 :per-page 2})) => 0)
                                     (fact "The count of transactions works properly."
                                           (blockchain/count-transactions mongo-bc {}) => (count (storage/query transaction-store {}))
                                           (blockchain/count-transactions mongo-bc {:currency "mongo"}) => (count (storage/query transaction-store {:currency "mongo"}))))))))
