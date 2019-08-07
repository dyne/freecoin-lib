(ns freecoin-lib.test.sawtooth
  (:require [midje.sweet :refer [facts => fact truthy throws]]
            [freecoin-lib
             [sawtooth :as saw]]
            [taoensso.timbre :as log]))

(facts "Check that we can decode the payload"
       (fact "We can decode a test payload"
             (let [payload "CAESPwokc2F3dG9vdGguY29uc2Vuc3VzLmFsZ29yaXRobS52ZXJzaW9uEgMwLjEaEjB4ZmU0NTA4YjNiODZlYjkyNw"]
               (saw/parse-payload payload) => 8))

       (fact "We can encode a payload map and then decode it and we get the same result"
             (let [payload-map {"script" "some-script"
                                "data" "some-data" 
                                "keys" "some-keys"
                                "context_id" "context-id-1"}
                   encoded-payload (saw/encode-payload payload-map)]
               (saw/parse-payload encoded-payload) => payload-map)))

