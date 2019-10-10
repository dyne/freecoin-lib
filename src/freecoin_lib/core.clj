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

(ns freecoin-lib.core
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]
            [fxc.core :as fxc]
            [freecoin-lib.db
             [tag :as tag]]
            [clj-storage.core :as storage]
            [freecoin-lib
             [utils :as utils]
             [config :as config]]
            [schema.core :as s]
            [freecoin-lib.schemas :refer [StoresMap
                                          RPCconfig]]
            [clj-btc
             [core :as btc]
             [config :as btc-conf]]
            [failjure.core :as f]
            [monger.conversion :refer [from-db-object]]
            [clj-time.core :as t]
            monger.joda-time
            monger.json)
  (:import [org.joda.time DateTimeZone]))

(DateTimeZone/setDefault DateTimeZone/UTC)

(defprotocol Blockchain
  ;; blockchain identifier
  (label [bk])

  ;; account
  (import-account [bk account-id secret])
  (create-account [bk name])
  (list-accounts [bk])

  (get-address [bk account-id])
  (create-address [bk account-id])
  (get-balance [bk account-id])
  (get-total-balance [bk])

  ;; transactions
  (list-transactions [bk params])
  (get-transaction   [bk txid])
  (create-transaction  [bk from-account-id amount to-account-id params])
  (update-transaction [bk txid fn])
  (move [bk from-account-id amount to-account-is params])
  (count-transactions [bk params])

  ;; tags
  (list-tags     [bk params])
  (get-tag       [bk name params])
  (create-tag    [bk name params])
  (remove-tag    [bk name])
  (count-tags    [bk params])

  ;; vouchers
  (create-voucher [bk account-id amount expiration secret])
  (redeem-voucher [bk account-id voucher])
  (list-vouchers  [bk])

  ;; petitions
  (create-petition [bk json])
  (sign-petition [bk petition-id json])
  (tally-petition [bk petition-id json])
  (count-petition [bk petition-id])
  (get-petition [bk petition-id]))

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

(defn merge-params [params f name updater]
  (if-let [request-value (params name)]
    (let [resolved-param (updater request-value)
          resolved-key (-> resolved-param first key)]
      (if (and (keyword? resolved-key) (resolved-key f))
        (merge-with merge f resolved-param)
        (merge f resolved-param)))
    f))

(defn add-transaction-list-params [request-params]
  (reduce-kv (partial merge-params request-params)
             {}
             {:to
              (fn [v] {:timestamp {"$lt" v}})
              :from
              (fn [v] {:timestamp {"$gte" v}})
              :account-id
              (fn [v] {"$or" [{:from-id v} {:to-id v}]})
              :tags
              (fn [v] {:tags {"$in" v}})
              :currency
              (fn [v] {:currency v})
              :description
              (fn [v] {:description v})}))

(defn add-tags-list-params [request-params]
  (reduce-kv (partial merge-params request-params)
             {}
             {:account-id
              (fn [v] {"$or" [{:from-id v} {:to-id v}]})}))

;; inherits from Blockchain and implements its methods
(s/defrecord Mongo [label :- s/Str stores-m :- StoresMap]
  Blockchain
  (label [bk]
    label)

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
      (- received sent)))

  (list-transactions [bk {:keys [page per-page tags from to] :as params}]
    ;; TODO extract
    (let [limit 100
          first-page 0
          default-items 10]
      (log/debug "getting transactions" params)
      (if (and per-page (> per-page limit))
        (f/fail (str "Cannot request more than " limit " transactions."))
        (let [current-page (atom first-page)
              items-per-page (atom default-items)]
          (when page (reset! current-page page))
          (when per-page (reset! items-per-page per-page)) 
          (storage/list-per-page (:transaction-store stores-m)
                                 (-> params
                                     (dissoc :page :per-page) 
                                     add-transaction-list-params)
                                 @current-page
                                 @items-per-page)))))

  (get-transaction   [bk txid]
    (let [response (storage/query (:transaction-store stores-m) {:transaction-id txid})]
      (if (and (first response) (:amount (first response)))
        (first response)
        (f/fail "Not found"))))

  ;; We need to create the tags through transactions and not on their own otherwie the amount and count aggregation to list them wont work
  #_(create-tag 
    )
  
  ;; TODO: get rid of account-ids and replace with wallets
  (create-transaction  [bk from-account-id amount to-account-id params]
    (f/if-let-ok? [parsed-amount (utils/string->Decimal128 amount)]
      ;; FIXME: oh no timestamp was saved as a string
      (let [timestamp (if-let [time (:timestamp params)] time (t/now))
            tags (or (:tags params) [])
            transaction-id (or (:transaction-id params) (fxc/generate 32))
            description (or (:description params) "")
            transaction {:_id (str timestamp "-" from-account-id)
                         :currency label
                         :timestamp timestamp
                         :from-id from-account-id
                         :to-id to-account-id
                         :tags tags
                         :amount parsed-amount
                         :amount-text amount
                         :transaction-id transaction-id
                         :description description}]
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
        (->
         (:transaction-store stores-m)
         (storage/store! :_id transaction)
         (update :amount #(from-db-object % true))))
      ;; In this case it is a failure
      parsed-amount))

  (update-transaction [bk txid fn]
    (storage/update! (:transaction-store stores-m) {:transaction-id txid} fn))

  (count-transactions [bk params] 
    (storage/count* (:transaction-store stores-m) params))
  
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
                 :amount amount
                 :created-by (:created-by tag)
                 :created (:created tag)}))
            tags)))

  (get-tag [bk name params]
    (first (filter #(= name (:tag %)) (list-tags bk params))))

  (count-tags [bk params] 
    (storage/count* (:tag-store stores-m) params))
  
  (create-voucher [bk account-id amount expiration secret] nil)

  (redeem-voucher [bk account-id voucher] nil))

(s/defn ^:always-validate new-mongo
  "Check that the blockchain is available, then return a record"
  [currency :- s/Str stores-m :- StoresMap]
  (s/validate Mongo (map->Mongo {:label currency :stores-m stores-m})))

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
    (let [now (t/now) 
          tags (or (:tags params) #{})
          description (or (:description params) "")
          transaction {:transaction-id (str now "-" from-account-id)
                       :currency "INMEMORYBLOCKCHAIN"
                       :timestamp now
                       :from-id from-account-id
                       :to-id to-account-id
                       :tags tags
                       :amount amount
                       :description description}]

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

(defn- with-error-response [response]
  (if (get response "code")
    (f/fail (get response "message"))
    response))

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
    (with-error-response
      (btc/getnewaddress :account name :config rpc-config)))  

  (list-accounts [bk]
    (with-error-response
      (btc/listaccounts :config rpc-config)))
  
  (get-address [bk account-id]
    (with-error-response
      (btc/getaddressesbyaccount :config rpc-config
                                 :account account-id)))

  (create-address [bk account-id]
    (with-error-response
      ;; TODO: do we need to specify account too?
      (btc/getnewaddress :config rpc-config
                         :account (or account-id ""))))

  (get-balance [bk account-id]
    "For the total balance account id has to be nil"
    (with-error-response
      (btc/getbalance :config rpc-config
                      :account account-id)))

  (get-total-balance [bk]
    (with-error-response
      (get-balance bk nil)))
  
  (list-transactions [bk params]
    "Returns up to [count] most recent transactions skipping the first [from] transactions for account [account]. If [account] not provided it'll return recent transactions from all accounts. When parameter :received-by-address is present the rest of the options will be ignored and a call to getreceivedbyaddress will be done for the particular address."
    (let [{:keys [account-id count from received-by-address]} params]
      (with-error-response
        (if received-by-address
          (btc/listreceivedbyaddress :config rpc-config)
          ;; FIX: Only transactions from the local wallet command.
          ;; Get raw transaction, get block command.
          ;; Node is full node, operates on local wallet.
          (btc/listtransactions :config rpc-config
                                :account account-id
                                :count count
                                :from from)))))
  (get-transaction   [bk txid]
    (f/if-let-failed? [response (with-error-response (btc/gettransaction :config rpc-config
                                                                         :txid txid))]
      (f/fail (:message response))
      (if (get response "amount")
        response
        ;; try raw transaction
        (let [raw-transaction (btc/getrawtransaction :config rpc-config
                                                     :txid txid
                                        ;:verbose true
                                                     )]
          (btc/decoderawtransaction :config rpc-config
                                    :hex-string raw-transaction)))))
  (create-transaction  [bk from-account-id amount to-account-id params]
    (try
      (with-error-response (btc/sendfrom :config rpc-config
                                         :fromaccount from-account-id
                                         :amount (utils/string->BigDecimal amount)
                                         :tobitcoinaddress to-account-id
                                         :comment (or (:comment params) "")
                                         :commentto (or (:commentto params) "")))
      (catch java.lang.AssertionError e
        (log/error "ERROR " e)
        (f/fail "No transaction possible. Error: " e))
      (catch java.lang.Exception e
        (log/error "Exception " e)
        (f/fail "Transaction amount too small: " e))))

  ;; ATTENTION: if the to-account or from-account dont exist they will be created
  (move [bk from-account-id amount to-account-id params]
    (with-error-response
      (btc/move :config rpc-config
                :fromaccount from-account-id
                :amount amount
                :toaccount to-account-id
                :comment (:comment params)))))

(s/defn ^:always-validate new-btc-rpc
  ([currency :- s/Str]
   (-> (config/create-config)
       (config/rpc-config)
       (new-btc-rpc)))
  ([currency :- s/Str 
    rpc-config-path :- s/Str]
   (f/if-let-ok? [rpc-config (f/try* (btc-conf/read-local-config rpc-config-path))] 
     (s/validate BtcRpc (map->BtcRpc {:label currency
                                      :rpc-config (dissoc rpc-config :txindex :daemon :debug)}))
     (f/fail (str "The blockchain configuration could not be loaded from " rpc-config-path)))))
