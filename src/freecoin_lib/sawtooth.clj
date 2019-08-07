;; Freecoin-lib - library to facilitate blockchain functions

;; part of Decentralized Citizen Engagement Technologies (D-CENT)
;; R&D funded by the European Commission (FP7/CAPS 610349)

;; Copyright (C) 2019- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>

;; Freecoin-lib is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; Freecoin-lib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify Freecoin-lib, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(ns freecoin-lib.sawtooth
  (:require [schema.core :as s]
            [freecoin-lib.schemas :refer [RestApiConf Payload]]
            [clj-http.client :as client]
            [taoensso.timbre :as log]
            [failjure.core :as f]
            [clj-cbor.core :as cbor]
            [cheshire.core :as json]
            [freecoin-lib.core :as freecoin]
            [buddy.hashers :as hashers])
  (:import [java.util Base64]
           [sawtooth.sdk.signing Secp256k1Context]
           [sawtooth.sdk.signing Signer]
           [sawtooth.sdk.protobuf TransactionHeader]
           [sawtooth.sdk.protobuf Transaction]
           [com.google.protobuf ByteString]
           [sawtooth.sdk.protobuf BatchHeader]
           [sawtooth.sdk.protobuf Batch]))

(defonce context (new Secp256k1Context))
(def private-key  (.newRandomPrivateKey context))
(def signer (new Signer context private-key))

(defn parse-payload [payload]
  (let [base64-decoded-payload (.decode (Base64/getDecoder) payload)]
    (cbor/decode base64-decoded-payload)))

(defn encode-payload [payload-m]
  (let [cbor (json/generate-cbor payload-m)]
    (.encode (Base64/getEncoder) cbor)))

(defn- create-transaction-header [signer payload-bytes]
  (let [inputs-outputs ["879e1d"]]
  (doto (TransactionHeader/newBuilder)
    (.setSignerPublicKey (.hex (.getPublicKey signer)))
    (.setFamilyName "zenroom")
    (.setFamilyVersion "1.0")
    (.addInputs inputs-outputs)
    (.addOutputs inputs-outputs)
    (.setPayloadSha512 (hashers/derive payload-bytes))
    (.setBatchPublicKey (.hex (.getPublicKey signer)))
    (.setNonce (.toString (java.util.UUID/randomUUID)))
    (.build))))

(defn- create-transaction [payload-bytes header signer]
  (let [signature (.sign signer (.toByteArray header))]
    (doto (Transaction/newBuilder)
      (.setHeader (.toByteString header))
      (.setPayload (ByteString/copyFrom payload-bytes))
      (.setHeaderSignature signature)
      (.build))))

(defn- create-batch-header [signer transaction]
  (doto (BatchHeader/newBuilder)
    (.setSignerPublicKey (.hex (.getPublicKey signer)))
    (.addAllTransactionIds '((.getHeaderSignature transaction)))
    (.build)))

(defn- create-batch [batch-header transactions]
  (let [batch-signature (.sign signer (.toByteArray batch-header))]
        (doto (Batch/newBuilder)
          (.setHeader (.toByteString batch-header))
          (.addAllTransactions transactions)
          (.setHeaderSignature batch-signature)
          (.build))))

(s/defrecord Sawtooth [label :- s/Str
                       restapi-conf :- RestApiConf]
  freecoin/Blockchain
  (label [bk]
    label)
  
  (list-transactions [bk params]
    ;; TODO: add paging parameters
    (let [response (client/get (str (:host restapi-conf) "/transactions") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (:body response)
        (f/fail "The sawtooth request responded with " (:status response)))))
  
  (get-transaction [bk txid]
    (let [response (client/get (str (:host restapi-conf) "/transactions/" txid) {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          (update-in body ["data" "payload"] #(parse-payload %)))
        (f/fail "The sawtooth request responded with " (:status response)))))

  ;; Implement the create like https://github.com/DECODEproject/sawroom/blob/master/tp/processor/handler.py#L58.
  ;; TODO: should I rename from sawtooth to the new name?
  (create-transaction  [bk from-account-id amount to-account-id {:keys [script data keys context-id]}]
    (let [payload-map {"script" script
                       "data" data
                       "keys" keys
                       "context_id" context-id}]
      (f/if-let-failed? [validation-error (f/try* (s/validate Payload (log/spy payload-map)))]
        (f/fail (f/message validation-error))
        (let [payload-bytes (encode-payload payload-map)
              transaction-header (create-transaction-header signer payload-bytes)
              transaction (create-transaction payload-bytes transaction-header signer)
              batch-header (create-batch-header signer transaction)
              batch (create-batch batch-header [transaction])
              response (client/post (str (:host restapi-conf) "/batches")
                                    {:headers  {:Content-Type "application/octet-stream"}
                                     :body batch})]
          (if (= 202 (:status (log/spy response)))
            (log/spy (:body response))
            (f/fail "The sawtooth request responded with " (:status response))))))))

(s/defn ^:always-validate new-sawtooth
  [currency :- s/Str
   restapi-conf :- RestApiConf]
  (s/validate Sawtooth (map->Sawtooth {:label currency
                                       :restapi-conf restapi-conf})))
