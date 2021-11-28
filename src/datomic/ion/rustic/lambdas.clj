(ns datomic.ion.rustic.lambdas
  (:require
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]))

(defn p<
  ([x] (prn x) x)
  ([msg x] (prn msg) (p< x)))


(defn get-subs-by-email
  "Lambda ion that returns subs matching email."
  [{:keys [input]}]
  (-> (rustic/get-db)
      (rustic/get-subs-by-email (json/read-str input)
                                [:sub/email :sub/feed-url])
      edn/write-str))

(defn register-sub
  [{:keys [input]}]
  (let [{:keys [email feed-url]} (json/read-str input :key-fn keyword)]
    (-> (rustic/get-connection)
        (rustic/register-sub email feed-url)
        edn/write-str)))
