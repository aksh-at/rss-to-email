(ns datomic.ion.rsstoemail.http
  (:require
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [datomic.ion.cast :as cast]
   [datomic.ion.rsstoemail :as rsstoemail]
   [datomic.ion.rsstoemail.auth :as auth]
   [datomic.ion.rsstoemail.edn :as edn]
   [datomic.ion.lambda.api-gateway :as apigw]))

;; Helpers

(defn edn-response
  [body]
  {:status 200
   :headers {"Content-Type" "application/edn"
             "Access-Control-Allow-Headers" "Content-Type,Authorization,X-Amz-Date,X-Api-Key,X-Amz-Security-Token"
             "Access-Control-Allow-Origin" "*"
             "Access-Control-Allow-Methods" "OPTIONS,POST,GET"}
   :body body})

(defn read-json-stream
  [input-stream]
  (some-> input-stream io/reader (java.io.PushbackReader.) (json/read :key-fn keyword)))

;; Definitions

(defn request-manage
  [{:keys [headers body]}]
  (let [{:keys [email]} (read-json-stream body)]
    (-> (rsstoemail/get-connection)
        (rsstoemail/request-manage email)
        edn/write-str
        edn-response)))

(defn get-current-subs
  [{:keys [headers body]}]
  (let [{:keys [token]} (read-json-stream body)
        {:keys [email]} (auth/decode-jwt-claim token)]
    (when (auth/valid-jwt? token)
      (-> (rsstoemail/get-connection)
          (rsstoemail/get-subs-by-email email)
          edn/write-str
          edn-response))))

(defn unsubscribe
  [{:keys [headers body]}]
  (let [{:keys [token feed-url]} (read-json-stream body)
        {:keys [email]} (auth/decode-jwt-claim token)]
    (when (auth/valid-jwt? token)
      (-> (rsstoemail/get-connection)
          (rsstoemail/unsubscribe email feed-url)
          edn/write-str
          edn-response))))

(defn request-sub
  [{:keys [headers body]}]
  (let [{:keys [email feed-url]} (read-json-stream body)]
    (-> (rsstoemail/get-connection)
        (rsstoemail/request-sub email feed-url)
        edn/write-str
        edn-response)))

(defn register-sub
  [{:keys [headers body]}]
  (let [{:keys [token feed-url]} (read-json-stream body)]
    (cast/event {:msg "Registering sub." :token token :feed-url feed-url})
    (def email (:email (auth/decode-jwt-claim token)))
    (if (auth/valid-jwt? token)
      (-> (rsstoemail/get-connection)
          (rsstoemail/register-sub email feed-url)
          edn/write-str
          edn-response)
      (-> :invalid
          edn/write-str
          edn-response))))

;; Ionized proxies

(def request-manage-lambda-proxy
  (apigw/ionize request-manage))

(def get-current-subs-lambda-proxy
  (apigw/ionize get-current-subs))

(def unsubscribe-lambda-proxy
  (apigw/ionize unsubscribe))

(def request-sub-lambda-proxy
  (apigw/ionize request-sub))

(def register-sub-lambda-proxy
  (apigw/ionize register-sub))

