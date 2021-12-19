(ns datomic.ion.rustic.http
  (:require
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.lambda.api-gateway :as apigw]))

(defn edn-response
  [body]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body body})

(defn read-json-stream
  [input-stream]
  (some-> input-stream io/reader (java.io.PushbackReader.) (json/read :key-fn keyword)))

(defn register-sub
  [{:keys [header body]}]
  (let [{:keys [email feed-url]} (read-json-stream body)]
    (-> (rustic/get-connection)
        (rustic/register-sub email feed-url)
        edn/write-str
        edn-response)))

(def register-sub-lambda-proxy
  (apigw/ionize register-sub))

