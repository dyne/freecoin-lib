(ns freecoin-lib.test.sawtooth
  (:require [midje.sweet :refer [facts => fact truthy throws]]
            [freecoin-lib
             [schemas :as fc]
             [sawtooth :as saw]]))

(facts "Check that we can decode the payload"
       (fact "We can decode a test payload"
             (let [payload "CAESPwokc2F3dG9vdGguY29uc2Vuc3VzLmFsZ29yaXRobS52ZXJzaW9uEgMwLjEaEjB4ZmU0NTA4YjNiODZlYjkyNw"]
               (saw/parse-payload payload) => 8)))

