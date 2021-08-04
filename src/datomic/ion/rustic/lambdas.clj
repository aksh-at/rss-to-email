(ns datomic.ion.rustic.lambdas
  (:require
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]))

(defn get-subs-by-email
  "Lambda ion that returns subs matching email."
  [{:keys [input]}]
  (-> (rustic/get-db)
      (rustic/get-subs-by-email (-> input json/read-str keyword)
                                [:sub/email :sub/rss])
      edn/write-str))

