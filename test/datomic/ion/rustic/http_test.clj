(ns datomic.ion.rustic.http-test
  (:require
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.test :as t]
   [clojure.data.json :as json]
   [datomic.ion.rustic.test-fixtures :as tf]
   [datomic.ion.rustic.db-utils :as db-utils]
   [datomic.ion.rustic.http :as http]
   [datomic.ion.rustic.lambdas :as lambdas]))

(tf/test-setup)

(def mem-conf-sub (atom {}))

(defn reset-conf-sub
  []
  (reset! mem-conf-sub {}))

(defn mock-conf-sub
  [email feed-url link]
  (swap! mem-conf-sub assoc :email email :feed-url feed-url :link link))


(defn- register-and-get
  [email url]
  (http/request-sub {:body (char-array (json/write-str {:email email, :feed-url url}))})
  (def link (get @mem-conf-sub :link))
  (def token (last (str/split link #"=")))
  (http/register-sub {:body (char-array (json/write-str {:token token, :feed-url url}))})
  (->> {:input (json/write-str email)}
       (lambdas/get-subs-by-email)
       edn/read-string
       (sort-by #(-> % first :sub/feed-url))
       doall))

(t/deftest subs-tests
  (with-redefs [datomic.ion.rustic.mailer/send-sub-confirmation mock-conf-sub]
    (t/testing "register subs works"
      (t/is (= (register-and-get "abc" "f1") [[#:sub{:email "abc", :feed-url "f1"}]])
            (t/is (= (register-and-get "abc" "f2") [[#:sub{:email "abc", :feed-url "f1"}] [#:sub{:email "abc", :feed-url "f2"}]]))))))
