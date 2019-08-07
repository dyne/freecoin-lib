;; Freecoin-lib - library to facilitate blockchain functions


;; Copyright (C) 2015- Dyne.org foundation

;; Sourcecode designed, written and maintained by
;; Aspasia Beneti <aspra@dyne.org>
;; Denis Roio <jaromil@dyne.org>

;; This program is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.

;; Additional permission under GNU AGPL version 3 section 7.

;; If you modify this Program, or any covered work, by linking or combining it with any library (or a modified version of that library), containing parts covered by the terms of EPL v 1.0, the licensors of this Program grant you additional permission to convey the resulting work. Your modified version must prominently offer all users interacting with it remotely through a computer network (if your version supports such interaction) an opportunity to receive the Corresponding Source of your version by providing access to the Corresponding Source from a network server at no charge, through some standard or customary means of facilitating copying of software. Corresponding Source for a non-source form of such a combination shall include the source code for the parts of the libraries (dependencies) covered by the terms of EPL v 1.0 used as well as that of the covered work.

(defproject org.clojars.dyne/freecoin-lib "1.5.0-SNAPSHOT"  
  :description "Freecoin digital currency toolkit - core library"
  :url "https://freecoin.dyne.org"
  
  :license {:author "Dyne.org Foundation"
            :email "foundation@dyne.org"
            :year 2017
            :key "gpl-3.0"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]

                 [environ "1.1.0"]
                 [clojure-humanize "0.2.2"]

                 ;; storage
                 [org.clojars.dyne/clj-storage "0.10.0"]

                 ;; fxc secret sharing protocol
                 [org.clojars.dyne/fxc "0.5.0"]

                 ;; config etc.
                 [org.clojars.dyne/auxiliary "0.4.0"]

                 ;; Data validation
                 [prismatic/schema "1.1.9"]

                 ;; Bitcoin lib
                 [clj-frc "0.1.2"]

                 ;; error handling
                 [failjure "1.3.0"]

                 ;; Use mongo bson data types like Decimal128
                 [org.mongodb/mongodb-driver "3.8.2"]

                 ;; Needed for monger.json
                 [cheshire "5.8.1"]

                 ;; http client for sawtooth restapi
                 [clj-http "3.10.0"]

                 ;;  Concise Binary Object Representation
                 [mvxcvi/clj-cbor "0.7.2"]

                 ;; Sawtooth-jdk
                 [local/sawtooth-sdk-signing "v0.1.2"]
                 [local/sawtooth-sdk-protos "v0.1.2"]

                 ;; For creating a sha512 hash
                 [buddy/buddy-hashers "1.4.0"]]

  :repositories {"local" "file:maven_repository"}
  :source-paths ["src"]
  :resource-paths ["resources" "test-resources" "file:maven_repository"]
  :jvm-opts ["-Djava.security.egd=file:/dev/random"
             ;; use a proper random source (install haveged)

             "-XX:-OmitStackTraceInFastThrow"
             ;; prevent JVM exceptions without stack trace
             ]
  :env [[:base-url "http://localhost:8000"]]
  :profiles {:dev [:dev-common :dev-local]
             :dev-common {:dependencies [[midje "1.9.2"]]
                          :repl-options {:init-ns freecoin-lib.core}
                          :plugins [[lein-midje "3.1.3"]]}}
  :aliases {"test"  ["midje"]}
  :plugins [[lein-environ "1.0.0"]])
