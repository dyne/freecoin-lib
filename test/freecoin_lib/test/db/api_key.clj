;; Freecoin-lib - library to facilitate blockchain functions

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2018- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; Freecoin-lib is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; Freecoin-lib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify Freecoin-lib, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns freecoin-lib.test.db.api-key
  (:require [midje.sweet :refer :all]
            [clj-storage.db.mongo :as mongo]
            [freecoin-lib.db 
             [api-key :as ak]
             [freecoin :as freecoin]]
            [clj-storage.test.db.test-db :as test-db]
            [taoensso.timbre :as log]))

(against-background [(before :contents (test-db/setup-db))
                     (after :contents (test-db/teardown-db))]
                    
                    (facts "Check that duplicate api-key creation is restricted by the indeces."
                           (let [db (test-db/get-test-db)
                                 stores-m (freecoin/create-freecoin-stores db)
                                 apikey-store (:apikey-store stores-m)]

                             (fact "Create an API KEY pair"
                                   (ak/create-apikey! {:apikey-store apikey-store
                                                       :client-app "app-1"
                                                       :api-key "key-1"}) => {:api-key "key-1" :client-app "app-1"})
                             (fact "Try to create a new api-key pair with the same client-app, throws an exception."
                                   (ak/create-apikey! {:apikey-store apikey-store
                                                       :client-app "app-1"
                                                       :api-key "key-2"})) => (throws com.mongodb.DuplicateKeyException)
                             (fact "The latter apikey pair was not persisted on the DB."
                                   (:api-key (ak/fetch-by-client-app apikey-store "app-1")) => "key-1"
                                   (:client-app (ak/fetch-by-api-key apikey-store "key-1")) => "app-1"
                                   (ak/fetch-by-api-key apikey-store "key-2") => nil)

                             (fact "Also no nil ones were created."
                                   (ak/fetch-by-api-key apikey-store :null) => nil
                                   (ak/fetch-by-client-app apikey-store :null) => nil)
                             
                             (fact "Try to create a new api-key pair with the same api key, throws an exception."
                                     (ak/create-apikey! {:apikey-store apikey-store
                                                         :client-app "app-2"
                                                         :api-key "key-1"})) => (throws com.mongodb.DuplicateKeyException)
                             (fact "The latter apikey pair was not persisted on the DB."
                                   (ak/fetch-by-client-app apikey-store "app-2") => nil
                                   (:client-app (ak/fetch-by-api-key apikey-store "key-1")) => "app-1")

                             (fact "List all apikey pairs so far."
                                   (ak/fetch-all apikey-store) => [{:api-key "key-1" :client-app "app-1"}])

                             (fact "Now create another new pair."
                                   (ak/create-apikey! {:apikey-store apikey-store
                                                         :client-app "another-app"
                                                         :api-key "another-key"}) => {:api-key "another-key" :client-app "another-app"}))))
