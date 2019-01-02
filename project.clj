(defproject org.clojars.dyne/freecoin-lib "1.3.0-SNAPSHOT"  
  :description "Freecoin digital currency toolkit"
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
                 [org.clojars.dyne/clj-storage "0.9.0"]

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
                 [cheshire "5.8.1"]]

  :source-paths ["src"]
  :resource-paths ["resources" "test-resources"]
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
