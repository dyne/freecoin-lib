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

(ns freecoin-lib.test.db.tag
  (:require [midje.sweet :refer :all] 
            [freecoin-lib.test.db.test-db :as test-db]
            [freecoin-lib
             [utils :as utils]
             [core :as blockchain]]
            [freecoin-lib.db
             [tag :as tag]
             [freecoin :as freecoin]]
            [taoensso.timbre :as log]
            [simple-time.core :as time]))

(against-background [(before :contents (test-db/setup-db))
                     (after :contents (test-db/teardown-db))]

                    (facts "Create some tags"
                           (let [stores-m (freecoin/create-freecoin-stores (test-db/get-test-db))
                                 mongo-bc (blockchain/new-mongo (log/spy stores-m))
                                 tag-store (:tag-store stores-m)]
                             (blockchain/create-transaction mongo-bc "user1" 10 "user2" {:tags ["test-tag1"]})
                             (blockchain/create-transaction mongo-bc "user2" 10 "user2" {:tags ["test-tag2"]})
                             #_(fact "retrieve tags"

                                   (-> (blockchain/get-tag mongo-bc "test-tag1" {})
                                       (dissoc :created)) =>
                                   {:amount 10M, :count 1, :created-by "user1", :tag "test-tag1"}

                                   (-> (blockchain/get-tag mongo-bc "test-tag2" {})
                                       (dissoc :created)) => {:amount 10M, :count 1, :created-by "user2", :tag "test-tag2"})
                             
                             (fact "Retrieve tags for currency"
                                   (count (blockchain/list-tags mongo-bc {})) => 2
                                   (count (blockchain/list-tags mongo-bc {})) => 0))))
