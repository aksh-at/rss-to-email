(ns datomic.ion.rustic.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.test :as t]
   [clojure.string :as str]
   [datomic.ion.rustic.http :as http]
   [datomic.ion.rustic.lambdas :as lambdas]
   [datomic.ion.rustic.test-fixtures :as tf]))

(tf/test-setup)

(def mem-send-email (atom {}))

(defn mock-send-email
  [email subject-line body]
  (swap! mem-send-email assoc :email email :subject-line subject-line :body body))

(defn get-token
  []
  (->
   @mem-send-email
   (get :body)
   (str/split #"token=")
   last
   (str/split #">")
   first))

(defn- register-and-get
  [email url]
  (http/request-sub {:body (char-array (json/write-str {:email email, :feed-url url}))})
  ;; (println (get @mem-send-email :body))
  (def token (get-token))
  ;; (println token)
  (http/register-sub {:body (char-array (json/write-str {:token token, :feed-url url}))})
  (->> {:input (json/write-str email)}
       (lambdas/get-subs-by-email)
       edn/read-string
       (sort-by #(-> % first :sub/feed-url))
       doall))

(t/deftest subs-tests
  (with-redefs [datomic.ion.rustic.mailer/send-email mock-send-email]
    (t/testing "register subs works"
      (t/is (= (register-and-get "abc" "f1") [[#:sub{:email "abc", :feed-url "f1"}]]))
      (t/is (= (register-and-get "abc" "f2") [[#:sub{:email "abc", :feed-url "f1"}] [#:sub{:email "abc", :feed-url "f2"}]])))))
