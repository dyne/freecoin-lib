;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015-2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>
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

(ns freecoin-lib.schemas
  (:require [schema.core :as s]))

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
   :tag-store clj_storage.db.mongo.MongoStore})

(def RPCconfig
  {:rpcpassword s/Str
   :rpcuser s/Str
   (s/optional-key :testnet) s/Bool
   :rpcport s/Int
   :rpchost s/Str
   (s/optional-key :txindex) s/Int
   (s/optional-key :daemon) s/Int
   (s/optional-key :port) s/Int})
