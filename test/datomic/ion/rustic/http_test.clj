(ns datomic.ion.rustic.http-test
  (:require
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.test :as t]
   [clojure.string :as str]
   [datomic.ion.rustic.utils :as u]
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
      (t/is (= (register-and-get "user1" "f1") [[#:sub{:email "user1", :feed-url "f1"}]]))
      (t/is (= (register-and-get "user1" "f2") [[#:sub{:email "user1", :feed-url "f1"}] [#:sub{:email "user1", :feed-url "f2"}]])))))

(defn- get-subs
  [email]
  (http/request-manage {:body (char-array (json/write-str {:email email}))})
  ;; (println (get @mem-send-email :body))
  (def token (get-token))
  ;; (println token)
  (->>  {:body (char-array (json/write-str {:token token}))}
        http/get-current-subs
        :body
        edn/read-string
        (sort-by #(-> % first :sub/feed-url))
        doall))

(t/deftest manage-tests
  (with-redefs [datomic.ion.rustic.mailer/send-email mock-send-email]
    (t/testing "manage subs works"
      (register-and-get "user2" "f1")
      (register-and-get "user2" "f2")
      (t/is (= (get-subs "user2") [[#:sub{:email "user2", :feed-url "f1"}] [#:sub{:email "user2", :feed-url "f2"}]])))))
