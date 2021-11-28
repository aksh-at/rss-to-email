(ns datomic.ion.rustic.poller-test
  (:require
   [clojure.instant :as instant]
   [clojure.test :as t]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.test-fixtures :as tf]
   [datomic.ion.rustic.utils :as utils]))

(tf/test-setup)

(def rss-post "test/datomic/ion/rustic/fixtures/rss-post.edn")

(def atom-post "test/datomic/ion/rustic/fixtures/atom-post.edn")

;; (t/deftest find-last-date-tests
;;   (t/testing "works for rss"
;;     (m/with-mock _
;;       {:target :clojure.xml/parse
;;        :return #(-> % utils/p< edn/read utils/p<)}
;;       (t/is (= (-> rss-post utils/p< poller/find-rss-last-date) "adff")))))

(t/deftest find-last-date-tests
  (t/testing "works for rss"
    (t/is (=
           (-> rss-post edn/read poller/find-last-date)
           (instant/read-instant-date "2021-04-23T06:14:01.000-00:00"))))
  (t/testing "works for atom"
    (t/is (=
           (-> atom-post edn/read poller/find-last-date)
           (instant/read-instant-date "2021-11-22T00:00:00.000-00:00")))))
