(ns datomic.ion.rustic.mailer-test
  (:require
   [clojure.test :as t]
   [datomic.client.api :as d]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.rustic.mailer :as mailer]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.test-fixtures :as tf]))

(tf/test-setup)

(def rss-post "test/datomic/ion/rustic/fixtures/rss-post.edn")

(def atom-post "test/datomic/ion/rustic/fixtures/atom-post.edn")

(t/deftest get-homepage-tests
  (t/testing "works w/ prefix"
    (t/is (= (mailer/get-homepage-from-feed "https://ab.xyz/index.xml") "ab.xyz"))
    (t/is (= (mailer/get-homepage-from-feed "http://ab.xyz/index.xml") "ab.xyz")))
  (t/testing "works without prefix"
    (t/is (= (mailer/get-homepage-from-feed "ab.xyz/index.xml") "ab.xyz"))))


(t/deftest test-format-subject-line
  (def new-posts (-> rss-post edn/read (poller/get-new-posts nil)))
  (t/is (= (mailer/format-subject-line "https://ab.xyz/index.xml" new-posts) "2 new updates from ab.xyz")))


;; (t/deftest test-format-body
;;   (def new-posts (-> rss-post edn/read (poller/get-new-posts nil)))
;;   (t/is (= (mailer/format-body "https://ab.xyz/index.xml" new-posts) "2 new updates from ab.xyz")))
