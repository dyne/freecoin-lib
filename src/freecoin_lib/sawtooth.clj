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
            [freecoin-lib.schemas :refer [RestApiConf]]
            [clj-http.client :as client]
            [taoensso.timbre :as log]
            [failjure.core :as f]
            [clj-cbor.core :as cbor]
            [freecoin-lib.core :as freecoin])
  (:import [java.util Base64]))

(defonce petition-token (atom ""))

;; TODO: add mongo endpoints
(defn parse-payload [payload]
  (let [base64-decoded-payload (.decode (Base64/getDecoder) payload)]
    (cbor/decode base64-decoded-payload)))

(defn with-token [restapi-conf request username password]
  "The petition api works with an oath 2 token that can expire. This is a wrapper funcion that if returns a not authorized reponse, requests a a new token, stores it and adds it to the requst headers."
  (if (clojure.string/blank? @petition-token)
    (let [response (client/post (str (:sawtooth-api restapi-conf) "/token") {:as :json-string-keys
                                                                             :basic-auth [username password]})]
      (if (= 200 (:status response))
        (do (reset! petition-token (:token (:body response)))
            (with-token restapi-conf request username password))
        (f/fail "The sawtooth token request responded with " (:status response))))
    ;; TODO here wrap the request with the token header. Break request to url params etc. 
    (let [response ((fn [req] ) request)])))

(s/defrecord Sawtooth [label :- s/Str
                       restapi-conf :- RestApiConf]
  freecoin/Blockchain
  (label [bk]
    label)
  
  (list-transactions [bk params]
    ;; TODO: add paging parameters
    (let [response (client/get (str (:sawtooth-api restapi-conf) "/transactions") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (:body response)
        (f/fail "The sawtooth request responded with " (:status response)))))
  
  (get-transaction [bk txid]
    (let [response (client/get (str (:sawtooth-api restapi-conf) "/transactions/" txid) {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          (update-in body ["data" "payload"] #(parse-payload %)))
        (f/fail "The sawtooth request responded with " (:status response)))))

  ;; TODO add schema check for json
  (create-petition [bx json]
    (let [response (client/post (str (:petition-api restapi-conf) "/petitions/") {:as :json-string-keys})]
      (if (= 201 (:status response))
        (let [body (:body response)]
          body)
        (f/fail "The create petition request responded with " (:status response)))))
  
  (sign-petition [bx petition-id json]
    (let [response (client/post (str (:petition-api restapi-conf) "/petitions/" petition-id "/sign") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          body)
        (f/fail "The sign petition request responded with " (:status response)))))

  (tally-petition [bx petition-id json]
    (let [response (client/post (str (:petition-api restapi-conf) "/petitions/" petition-id "/tally") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          body)
        (f/fail "The tally petition request responded with " (:status response)))))

  (count-petition [bx petition-id]
    (let [response (client/get (str (:petition-api restapi-conf) "/petitions/" petition-id "/count") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          body)
        (f/fail "The count petition request responded with " (:status response)))))

  (get-petition [bx petition-id]
    (let [response (client/get (str (:petition-api restapi-conf) "/petitions/" petition-id "/tally") {:as :json-string-keys})]
      (if (= 200 (:status response))
        (let [body (:body response)]
          body)
        (f/fail "The get petition request responded with " (:status response))))))

(s/defn ^:always-validate new-sawtooth
  [currency :- s/Str
   restapi-conf :- RestApiConf]
  (s/validate Sawtooth (map->Sawtooth {:label currency
                                       :restapi-conf restapi-conf})))
