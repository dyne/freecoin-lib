(ns freecoin-lib.test-helpers.store
  (:require [freecoin-lib.db.mongo :as db]
            [freecoin-lib.core :as bc])
  ;; NOTE THE UNDERSCORE CHANGE IN NAMESPACE ON IMPORT
  ;; IMPORTANT else "class not found" occurs
  (:import [freecoin_lib.db.mongo MemoryStore]
           [freecoin_lib.core InMemoryBlockchain])
  )

(defprotocol TestStore
  (entry-count [s]
    "Total number of entries in the store")
  (summary [s]
    "A map providing some summary information about the store"))

(extend-protocol TestStore
  MemoryStore
  (entry-count [this] (count @(:data this)))
  (summary [this] {:entry-count (count @(:data this))}))

(extend-protocol TestStore
  InMemoryBlockchain
  (entry-count [this] nil)
  (summary [this] {:transaction-count (count @(:transactions-atom this))}))
