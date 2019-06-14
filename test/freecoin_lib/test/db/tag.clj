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

(ns freecoin-lib.test.db.tag
  (:require [midje.sweet :refer [against-background before after facts fact =>]] 
            [clj-storage.test.db.test-db :as test-db]
            [freecoin-lib
             [utils :as utils]
             [core :as blockchain]]
            [freecoin-lib.db.freecoin :as freecoin]
            [taoensso.timbre :as log]
            [clj-time.core :as t]))

(against-background [(before :contents (test-db/setup-db))
                     (after :contents (test-db/teardown-db))]

                    (facts "Create some tags"
                           (let [stores-m (freecoin/create-freecoin-stores (test-db/get-test-db))
                                 mongo-bc (blockchain/new-mongo "Testcoin" stores-m)]

                             (blockchain/create-transaction mongo-bc "participant-1" "10" "participant-2" {:tags ["project-1"]})
                             (blockchain/create-transaction mongo-bc "participant-1" "10" "participant-2" {:tags ["project-1" "project-2"]})
                             (blockchain/create-transaction mongo-bc "participant-2" "10" "participant-3" {:tags ["project-1" "project-2"]})
                             (blockchain/create-transaction mongo-bc "participant-1" "10" "participant-3" {:tags ["project-3"]})
                             (blockchain/create-transaction mongo-bc "participant-1" "10" "participant-2" {:tags ["project-3" "project-4" "project-5"]})
                             (blockchain/create-transaction mongo-bc "participant-3" "10" "participant-2" {:tags ["project-1"]})
                             
                             ;; We need to create the tags through transactions and not apart otherwie the amount and count aggregation to list them wont work
                             (fact "Counting tags works"
                                   (blockchain/count-tags mongo-bc {}) => 5)

                             (fact "Listing tags works"
                                   (:count (last (blockchain/list-tags mongo-bc {}))) => 4)
                             
                             (fact "Can get a tag by name"
                                   (:created-by (blockchain/get-tag mongo-bc "project-1" {})) => "participant-1")

                             ;; FIXME There is a bug with filtering tags by parameters while retrieving them.
                             ;; The reason is that the mongo query happens **per transaction** and then **unwinds** all tags and regroups them
                             ;; adding amounts also taken from the trancactions colummn. However we cannot use **match** on a separate collecion
                             ;; in this case the tag on instead of a transaciton. Explanation
                             ;; https://stackoverflow.com/questions/29387343/mongodb-filter-out-some-values-when-doing-unwind                             
                             #_(fact "Can get a tag with params"
                                     (blockchain/list-tags mongo-bc {:tag "project-6"}) => {})

                             ;; FIXME: this is curently implemented with aggregation and not pagination, which are two different calls
                             #_(fact "Retrieving transactions with and without paging works"
                                   (count (blockchain/list-tags mongo-bc {})) => 5
                                   
                                   (count (blockchain/list-tags mongo-bc {:page 0 :per-page 2})) => 2
                                   (count (blockchain/list-tagss mongo-bc {:page 1 :per-page 2})) => 2
                                   (count (blockchain/list-tags mongo-bc {:page 2 :per-page 2})) => 1
                                   (count (blockchain/list-tags mongo-bc {:page 3 :per-page 2})) => 0))))
