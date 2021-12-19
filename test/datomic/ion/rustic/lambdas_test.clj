(ns datomic.ion.rustic.lambdas-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :as t]
   [clojure.data.json :as json]
   [datomic.ion.rustic.test-fixtures :as tf]
   [datomic.ion.rustic.db-utils :as db-utils]
   [datomic.ion.rustic.http :as http]
   [datomic.ion.rustic.lambdas :as lambdas]))

(tf/test-setup)

(defn- register-and-get
  [email url]
  (http/register-sub {:body (char-array (json/write-str {:email email, :feed-url url}))})
  (->> {:input (json/write-str email)}
       (lambdas/get-subs-by-email)
       edn/read-string
       (sort-by #(-> % first :sub/feed-url))
       doall))

(t/deftest subs-tests
  (t/testing "register subs works"
    (t/is (= (register-and-get "abc" "f1") [[#:sub{:email "abc", :feed-url "f1"}]])
          (t/is (= (register-and-get "abc" "f2") [[#:sub{:email "abc", :feed-url "f1"}] [#:sub{:email "abc", :feed-url "f2"}]])))))
