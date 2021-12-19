(ns datomic.ion.rustic.http
  (:require
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.lambda.api-gateway :as apigw]))

(defn edn-response
  [body]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body body})

(defn register-sub
  [{:keys [header body]}]
  (let [{:keys [email feed-url]} (json/read-str body :key-fn keyword)]
    (-> (rustic/get-connection)
        (rustic/register-sub email feed-url)
        edn/write-str
        edn-response)))

(def register-sub-lambda-proxy
  (apigw/ionize register-sub))

