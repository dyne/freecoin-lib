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

(ns freecoin-lib.schemas
  (:require [schema.core :as s]
            [clj-storage.db.mongo :as m]))

(def schema_mongo
  {:port s/Num
   :host s/Str
   :db   s/Str})

(def schema_bitcoin_compatible
  {:host s/Str
   :port s/Num
   :pass s/Str})

(s/defschema Config
  {
   (s/required-key :mongo) schema_mongo

   (s/optional-key :faircoin)   schema_bitcoin_compatible

   (s/optional-key :bitcoin)    schema_bitcoin_compatible

   (s/optional-key :litecoin)   schema_bitcoin_compatible

   (s/optional-key :multichain) schema_bitcoin_compatible

   ;;  (s/optional-key :ethereum)
   ;;  {:socket s/Str}

   })

;; internal validator for mongo backend (used by core/new-mongo)
(s/defschema StoresMap
  {:wallet-store clj_storage.db.mongo.MongoStore
   :confirmation-store clj_storage.db.mongo.MongoStore
   :transaction-store clj_storage.db.mongo.MongoStore
   :tag-store clj_storage.db.mongo.MongoStore
   :apikey-store clj_storage.db.mongo.MongoStore})

(def RPCconfig
  {:rpcpassword s/Str
   :rpcuser s/Str
   (s/optional-key :testnet) s/Bool
   :rpcport s/Int
   :rpchost s/Str
   (s/optional-key :txindex) s/Int
   (s/optional-key :daemon) s/Int
   (s/optional-key :port) s/Int})
