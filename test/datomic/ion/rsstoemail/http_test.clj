(ns datomic.ion.rsstoemail.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.test :as t]
   [clojure.string :as str]
   [datomic.ion.rsstoemail.utils :as u]
   [datomic.ion.rsstoemail.http :as http]
   [datomic.ion.rsstoemail.lambdas :as lambdas]
   [datomic.ion.rsstoemail.test-fixtures :as tf]))

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

(defn- get-subs [token]
  (->>  {:body (char-array (json/write-str {:token token}))}
        http/get-current-subs
        :body
        edn/read-string
        :subs
        (sort-by #(-> % first :sub/feed-url))
        doall))

(defn- register-and-get-subs
  [email url]
  (http/request-sub {:body (char-array (json/write-str {:email email, :feed-url url}))})
  ;; (println (get @mem-send-email :body))
  (def token (get-token))
  ;; (println token)
  (http/register-sub {:body (char-array (json/write-str {:token token, :feed-url url}))})
  (get-subs token))

(t/deftest subs-tests
  (with-redefs [datomic.ion.rsstoemail.mailer/send-email mock-send-email]
    (t/testing "register subs works"
      (t/is (= (register-and-get-subs "user1" "f1") [[#:sub{:email "user1", :feed-url "f1"}]]))
      (t/is (= (register-and-get-subs "user1" "f2") [[#:sub{:email "user1", :feed-url "f1"}] [#:sub{:email "user1", :feed-url "f2"}]])))))

(defn- request-and-get-subs
  [email]
  (http/request-manage {:body (char-array (json/write-str {:email email}))})
  ;; (println (get @mem-send-email :body))
  (def token (get-token))
  ;; (println token)
  (get-subs token))

(defn- unsub
  [email feed-url]
  (http/request-manage {:body (char-array (json/write-str {:email email}))})
  (def token (get-token))
  (http/unsubscribe {:body (char-array (json/write-str {:token token :feed-url feed-url}))}))

(t/deftest manage-tests
  (with-redefs [datomic.ion.rsstoemail.mailer/send-email mock-send-email]
    (t/testing "manage subs works"
      (register-and-get-subs "user2" "f1")
      (register-and-get-subs "user2" "f2")
      (t/is (= (request-and-get-subs "user2") [[#:sub{:email "user2", :feed-url "f1"}] [#:sub{:email "user2", :feed-url "f2"}]]))
      (register-and-get-subs "user2" "f3")
      (t/is (= (request-and-get-subs "user2") [[#:sub{:email "user2", :feed-url "f1"}] [#:sub{:email "user2", :feed-url "f2"}] [#:sub{:email "user2", :feed-url "f3"}]]))
      (unsub "user2" "f2")
      (t/is (= (request-and-get-subs "user2") [[#:sub{:email "user2", :feed-url "f1"}] [#:sub{:email "user2", :feed-url "f3"}]])))))
