(ns freecoin-lib.test.sawtooth
  (:require [midje.sweet :refer [facts => fact truthy throws]]
            [freecoin-lib
             [sawtooth :as saw]]
            [taoensso.timbre :as log]))

(facts "Check that we can decode the payload"
       (fact "We can decode a test payload"
             (let [payload "CAESPwokc2F3dG9vdGguY29uc2Vuc3VzLmFsZ29yaXRobS52ZXJzaW9uEgMwLjEaEjB4ZmU0NTA4YjNiODZlYjkyNw"]
               (saw/parse-payload payload) => 8))

       ;; TODO: does this even make sense?
       #_(fact "We can encode a payload map and then decode it and we get the same result"
             (let [payload-map {"script" "some-script"
                                "data" "some-data" 
                                "keys" "some-keys"
                                "context_id" "context-id-1"}
                   encoded-payload (saw/encode-payload payload-map)]
               (saw/parse-payload encoded-payload) => payload-map))

       (fact "Check with example payload map"
             (let [payload-map  {"script" "Scenario 'coconut': \"Sign petition: the Citizen signs the petition, with his own keys, aggregating it with the credential and with the Credential Issuer public key\"\nGiven that I am known as 'identifier' \nand I have my keypair\nand I have a signed credential\nand I use the verification key by 'issuer_identifier'\nWhen I aggregate all the verification keys\nand I sign the petition 'petition'\nThen print all data\n", "data" "{\"issuer_identifier\":{\"verify\":{\"beta\":\"29eba39a909c54e8dfa11a5be712968224da0be97069c8a21d250754776b9a280736a2eb08e583ea733603555af1eb7e0282fcac366bb0df694df9d91471040961c144508c43ba27424a39a403e28c564bf1b8c61709b3f49cfcbc1a0566fdd2508b52f0acba942c32c6075fe0c189bcbf6ce995687e499da5ea41aee71fc23bb7220fe552a73218929e4b46979208f11e6c2fa131971830704edbe4128642a42a6018c2cd27c49200822857b928964c6ec6436808e53d26ca3627ce2801ea82\",\"alpha\":\"33fa6aaed642d9ddfb49f30d100e3683d4e97b8b4b74c857c4ac7d0a93726621dc27d6f244a99c9166bbae316d5105ef31b2aa18f92fd8d94b54469b689fce53c236795795490656ade95eb05251713f337fbe4dbd6669f70a4ccf09c8de4bd92f44e6376bd3325fde6ea6363481f433551f58a3d3295ee5a414a27f7b2121a3023d2d938d3aa326ad356293bb6a307b469d9bd42c78ff96ef2eb1e2cf0f3c025b4aa3c3d32ef230d94aca3ff3d311ad79a2f5fc8aca249b9d92f88050cd9f05\"}}}\n", "keys" "{\"credential\":{\"s\":\"044bde1713e2827ded64d89642154542b1615e69abcaef15768549092d693898fc7e24cb061cd0f58d986159f98f7a867f0915b9aa4bdada75ab2f4bc3b291c6c15181c2189d6f11f0d32c417bf7ce7a79dffa46d6e2c3b1d04fb07e9219936cdb\",\"curve\":\"bls383\",\"schema\":\"aggsigma\",\"h\":\"0418678dfd86e6efd8ad3e076a8339e407b1a084ca8c37c909031b08c73b75d6dfdfe0ef6794fd093c51156e8158eed7a35192456d9808eeddd6f3447a11911c9150873c409ff76d8f9c74ad3521a9293605e4eeab7e0251a10bb5fe0e6ab62510\",\"zenroom\":\"0.9\",\"encoding\":\"hex\"},\"identifier\":{\"private\":\"bac8dbb33f9c9cc815bb328e879fc5863052d13bf5f6c3e3c000c5fa0ff213ee\",\"public\":\"041a6bad479b23ea533770b505aa5fc43225323ceaa06bcaf7cccf0b16971f535fe06c805569cfaf8685a03b8988f6a25324ecf2618d66a2ed93095a0f13efcbd103ed9324ac6fadae8b825ed2f42261fee5ab9a622932c1667b240408bd183131\",\"curve\":\"bls383\",\"schema\":\"cred_keypair\",\"zenroom\":\"0.9\",\"encoding\":\"hex\"}}\n", "context_id" "aspra-text"}
                   payload-bytes (saw/encode-payload payload-map)
                   transaction-header (saw/create-transaction-header saw/signer (log/spy payload-bytes))

                   _ (log/info "Before constructikn g transaction")
                   transaction (saw/create-sawtooth-transaction payload-bytes (log/spy transaction-header) saw/signer)
                   batch-header (saw/create-batch-header saw/signer (log/spy transaction))
                   batch (saw/create-batch (log/spy batch-header) [transaction])]
               batch => truthy)))

