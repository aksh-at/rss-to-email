(ns datomic.ion.rustic.http
  (:require
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.auth :as auth]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.lambda.api-gateway :as apigw]))

;; Helpers

(defn edn-response
  [body]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body body})

(defn read-json-stream
  [input-stream]
  (some-> input-stream io/reader (java.io.PushbackReader.) (json/read :key-fn keyword)))

;; Definitions

(defn request-sub
  [{:keys [headers body]}]
  (let [{:keys [email feed-url]} (read-json-stream body)]
    (-> (rustic/get-connection)
        (rustic/request-sub email feed-url)
        edn/write-str
        edn-response)))

(defn register-sub
  [{:keys [headers body]}]
  (let [{:keys [token feed]} (read-json-stream body)
        {:keys [email]} (auth/decode-jwt-claim token)]
    (when (auth/valid-jwt? token)
      (-> (rustic/get-connection)
          (rustic/register-sub email feed)
          edn/write-str
          edn-response))))

;; Ionized proxies

(def request-sub-lambda-proxy
  (apigw/ionize request-sub))

(def register-sub-lambda-proxy
  (apigw/ionize register-sub))

