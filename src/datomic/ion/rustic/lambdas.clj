(ns datomic.ion.rustic.lambdas
  (:require
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]))


(defn get-subs-by-email
  [{:keys [input]}]
  (-> (rustic/get-db)
      (rustic/get-subs-by-email (json/read-str input)
                                [:sub/email :sub/feed-url])
      edn/write-str))



(defn poll-all
  [{:keys [input]}]
  (-> (rustic/get-connection)
      rustic/poll-all
      doall
      count
      str))
