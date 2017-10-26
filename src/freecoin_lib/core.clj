;; Freecoin - digital social currency toolkit

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2015-2017 Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Denis Roio <jaromil@dyne.org>
;; Aspasia Beneti <aspra@dyne.org>

;; With contributions by
;; Carlo Sciolla
;; Arjan Scherpenisse <arjan@scherpenisse.net>

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

(ns freecoin-lib.core
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]
            [fxc.core :as fxc]
            [freecoin-lib.db
             [tag :as tag]]
            [clj-storage.core :as storage]
            [clj-storage.db.mongo :as mongo]
            [freecoin-lib
             [utils :as utils]
             [config :as config]]
            [simple-time.core :as time]
            [schema.core :as s]
            [freecoin-lib.schemas :refer [StoresMap
                                          RPCconfig]]
            [clj-btc
             [core :as btc]
             [config :as btc-conf]]))

(defprotocol Blockchain
  ;; blockchain identifier
  (label [bk])

  ;; account
  (import-account [bk account-id secret])
  (create-account [bk name])
  (list-accounts [bk])

  (get-address [bk account-id])
  (get-balance [bk account-id])
  (get-total-balance [bk])

  ;; transactions
  (list-transactions [bk params])
  (get-transaction   [bk txid])
  (create-transaction  [bk from-account-id amount to-account-id params])

  ;; tags
  (list-tags     [bk params])
  (get-tag       [bk name params])
  (create-tag    [bk name params])
  (remove-tag    [bk name])

  ;; vouchers
  (create-voucher [bk account-id amount expiration secret])
  (redeem-voucher [bk account-id voucher])
  (list-vouchers  [bk]))
(defrecord voucher
    [_id
     expiration
     sender
     amount
     blockchain
     currency])

(defrecord transaction
    [_id
     emission
     broadcast
     signed
     sender
     amount
     recipient
     blockchain
     currency])

;; this is here just to explore how introspection works in clojure records
;; basically one could just explicit the string "STUB" where this is used
(defn recname
  "Return a string which is the name of the record class, uppercase.
Used to identify the class type."
  [record]
  (-> record
      class
      pr-str
      (str/split #"\.")
      last
      str/upper-case))

;; TODO
(defrecord nxt [server port])

(defn- normalize-transactions [list]
  (reverse
   (sort-by :timestamp
            (map (fn [{:keys [amount] :as transaction}]
                   (assoc transaction :amount (utils/long->bigdecimal amount)))
                 list))))

(defn merge-params [params f name updater]
  (if-let [request-value (params name)]
    (merge f (updater request-value))
    f))

(defn add-transaction-list-params [request-params]
  (reduce-kv (partial merge-params request-params)
             {}
             {:to
              (fn [v] {:timestamp {"$lt" v}})
              :from
              (fn [v] {:timestamp {"$gt" v}})
              :account-id
              (fn [v] {"$or" [{:from-id v} {:to-id v}]})
              :tags
              (fn [v] {:tags {"$in" v}})}))

(defn add-tags-list-params [request-params]
  (reduce-kv (partial merge-params request-params)
             {}
             {:account-id
              (fn [v] {"$or" [{:from-id v} {:to-id v}]})}))

;; inherits from Blockchain and implements its methods
(s/defrecord Mongo [stores-m :- StoresMap]
  Blockchain
  (label [bk]
    (keyword (recname bk)))

  (import-account [bk account-id secret]
    nil)

  (create-account [bk name]
    (let [secret (fxc/generate :url 64)
          uniqueid (fxc/generate :url 128)]
      {:account-id uniqueid
       :account-name name
       ;; TODO: establish a unique-id generation algo and cycle of
       ;; life; this is not related to the :email uniqueness
       :account-secret secret}
      ;; TODO: wrap all this with symmetric encryption using fxc secrets
      ))

  (get-address [bk account-id] nil)
  
  (get-balance [bk account-id]
    ;; we use the aggregate function in mongodb, sort of simplified map/reduce
    (let [received-map (first (storage/aggregate (:transaction-store stores-m) 
                                                 [{"$match" {:to-id account-id}}
                                                  {"$group" {:_id "$to-id"
                                                             :total {"$sum" "$amount"}}}]))
          sent-map  (first (storage/aggregate (:transaction-store stores-m)
                                              [{"$match" {:from-id account-id}}
                                               {"$group" {:_id "$from-id"
                                                          :total {"$sum" "$amount"}}}]))
          received (if received-map (:total received-map) 0)
          sent     (if sent-map (:total sent-map) 0)]
      (utils/long->bigdecimal (- received sent))))

  (list-transactions [bk params]
    (log/debug "getting transactions" params)
    (normalize-transactions
     (storage/query (:transaction-store stores-m) (add-transaction-list-params params))))

  (get-transaction   [bk txid]
    (storage/query (:transaction-store stores-m) {:transaction-id txid}))

  ;; TODO: get rid of account-ids and replace with wallets
  (create-transaction  [bk from-account-id amount to-account-id params]
    (let [timestamp (time/format (if-let [time (:timestamp params)] time (time/now)))
          tags (or (:tags params) [])
          transaction-id (:transaction-id params) 
          transaction {:_id (str timestamp "-" from-account-id)
                       :currency (or (:currency params) "MONGO")
                       :timestamp timestamp
                       :from-id from-account-id
                       :to-id to-account-id
                       :tags tags
                       :amount (utils/bigdecimal->long amount)
                       :transaction-id transaction-id}]
      ;; TODO: Maybe better to do a batch insert with
      ;; monger.collection/insert-batch? More efficient for a large
      ;; amount of inserts
      (doall (map #(tag/create-tag! {:tag-store (:tag-store stores-m) 
                                     :tag %
                                     :created-by from-account-id
                                     :created timestamp})
                  tags))
      ;; TODO: Keep track of accounts to verify validity of from- and
      ;; to- accounts
      (storage/store! (:transaction-store stores-m) :_id transaction)
      ))

  (list-tags [bk params]
    (let [by-tag [{:$unwind :$tags}]
          tags-params (apply conj by-tag (if (coll? params)
                                             params
                                             [params]))
          params (into tags-params [{:$group {:_id "$tags"
                                              :count {"$sum" 1}
                                              :amount {"$sum" "$amount"}}}])
          tags (storage/aggregate (:transaction-store stores-m)  params)]
      (mapv (fn [{:keys [_id count amount]}]
              (let [tag (tag/fetch (:tag-store stores-m) _id)]
                {:tag   _id
                 :count count
                 :amount (utils/long->bigdecimal amount)
                 :created-by (:created-by tag)
                 :created (:created tag)}))
            tags)))

  (get-tag [bk name params]
    (first (filter #(= name (:tag %)) (list-tags bk params))))

  (create-voucher [bk account-id amount expiration secret] nil)

  (redeem-voucher [bk account-id voucher] nil))

(s/defn ^:always-validate new-mongo
  "Check that the blockchain is available, then return a record"
  [stores-m :- StoresMap]
  (s/validate Mongo (map->Mongo {:stores-m stores-m})))

(defn in-memory-filter [entry params]
  true)

;;; in-memory blockchain for testing
(defrecord InMemoryBlockchain [blockchain-label transactions-atom accounts-atom tags-atom]
  Blockchain
  ;; identifier
  (label [bk]
    blockchain-label)

  ;; account
  (import-account [bk account-id secret]
    nil)
  (create-account [bk name]
    (let [secret (fxc/generate :url 64)
          uniqueid (fxc/generate :url 128)]
      {:account-id uniqueid
       :account-secret secret}))

  (get-address [bk account-id] nil)
  (get-balance [bk account-id]
    (let [all-transactions (vals @transactions-atom)
          total-withdrawn (->> all-transactions
                               (filter (comp (partial = account-id) :from-account-id))
                               (map :amount)
                               (reduce +))
          total-deposited (->> all-transactions
                               (filter (comp (partial = account-id) :to-account-id))
                               (map :amount)
                               (reduce +))]
      (- total-deposited total-withdrawn)))

  ;; transactions
  (list-transactions [bk params] (do
                                   (log/info "In-memory params:" params)
                                   (let [list (vals @transactions-atom)]
                                     (if (empty? params)
                                       list
                                       [(second list)]))))

  (get-transaction   [bk txid] nil)
  (create-transaction  [bk from-account-id amount to-account-id params]
    ;; to make tests possible the timestamp here is generated starting from
    ;; the 1 december 2015 plus a number of days that equals the amount
    (let [now (time/format (time/add-days (time/datetime 2015 12 1) amount))
          tags (or (:tags params) #{})
          transaction {:transaction-id (str now "-" from-account-id)
                       :currency "INMEMORYBLOCKCHAIN"
                       :timestamp now
                       :from-id from-account-id
                       :to-id to-account-id
                       :tags tags
                       :amount amount}]

      (doall (map #(swap! tags-atom assoc {:tag %
                                           :created-by from-account-id
                                           :created now})
                  tags))
      
      (swap! transactions-atom assoc (:transaction-id transaction) transaction)
      transaction))
  
  ;; vouchers
  (create-voucher [bk account-id amount expiration secret])
  (redeem-voucher [bk account-id voucher]))

(s/defn create-in-memory-blockchain
  ([label :- s/Keyword] (create-in-memory-blockchain label (atom {}) (atom {}) (atom {})))

  ([label :- s/Keyword transactions-atom :- clojure.lang.Atom accounts-atom :- clojure.lang.Atom tags-atom :- clojure.lang.Atom]
   (s/validate InMemoryBlockchain (map->InMemoryBlockchain {:blockchain-label label
                                                            :transactions-atom transactions-atom
                                                            :accounts-atom accounts-atom
                                                            :tags-atom tags-atom}))))

(s/defrecord BtcRpc [label :- s/Str
                     rpc-config :- RPCconfig]
  Blockchain
  (label [bk]
    label)

  (import-account [bk account-id secret]
    ;; TODO
    )

  (create-account [bk name]
    "Returns the address of the newely created account"
    (btc/getnewaddress :account name :config rpc-config))  

  (list-accounts [bk]
    (btc/listaccounts :config rpc-config))
  
  (get-address [bk account-id]
    (btc/getaddressesbyaccount :config rpc-config
                               :account account-id))

  (get-balance [bk account-id]
    "Fot the total balance account id has to be nil"
    (btc/getbalance :config rpc-config
                    :account account-id))

  (get-total-balance [bk]
    (get-balance bk nil))
  
  (list-transactions [bk params]
    "Returns up to [count] most recent transactions skipping the first [from] transactions for account [account]. If [account] not provided it'll return recent transactions from all accounts."
    (let [{:keys [account-id count from]} params]
      (btc/listtransactions :config rpc-config
                            :account account-id
                            :count count
                            :from from)))
  (get-transaction   [bk txid]
    (btc/gettransaction :config rpc-config
                        :txid txid))
  (create-transaction  [bk from-account-id amount to-account-id params]
    (btc/sendfrom :config rpc-config
                  :fromaccount from-account-id
                  :amount amount
                  :tobitcoinaddress (or
                                     (:to-address params)
                                     (first (get-address bk to-account-id))) 
                  :comment (:comment params)
                  :commentto (:comment-to params))))

(s/defn ^:always-validate new-btc-rpc
  ([currency :- s/Str]
   (-> (config/create-config)
       (config/rpc-config)
       (new-btc-rpc)))
  ([currency :- s/Str
    rpc-config-path :- s/Str]
   (let [rpc-config (btc-conf/read-local-config rpc-config-path)]
     (s/validate BtcRpc (map->BtcRpc {:label currency
                                      :rpc-config (dissoc rpc-config :txindex :daemon)})))))
