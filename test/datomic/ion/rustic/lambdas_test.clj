(ns datomic.ion.rustic.lambdas-test
  (:require
   [clojure.edn :as edn]
   [clojure.test :as t]
   [clojure.data.json :as json]
   [datomic.dev-local :as dl]
   [datomic.ion.rustic.db-utils :as db-utils]
   [datomic.ion.rustic.lambdas :as lambdas]))

(dl/divert-system {:system "datomic-4" :storage-dir :mem })

(def test-db-name "datomic-rustic-test")

(defn with-db [f]
  (db-utils/load-schemas)
  (f)
  (db-utils/delete-db))

(t/use-fixtures :once with-db)

(defn- register-and-get
  [email url]
  (lambdas/register-sub {:input (json/write-str {:email email, :feed-url url})})
  (->> {:input (json/write-str email)}
      (lambdas/get-subs-by-email)
      edn/read-string
      (sort-by #(-> % first :sub/feed-url))
      doall
      ))

(t/deftest subs-tests
  (t/testing "register subs works"
    (t/is (= (register-and-get "abc" "f1") [[#:sub{:email "abc", :feed-url "f1"}]])
    (t/is (= (register-and-get "abc" "f2") [[#:sub{:email "abc", :feed-url "f1"}] [#:sub{:email "abc", :feed-url "f2"}]])))))


