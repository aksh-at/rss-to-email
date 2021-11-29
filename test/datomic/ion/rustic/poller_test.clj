(ns datomic.ion.rustic.poller-test
  (:require
   [clojure.instant :as instant]
   [clojure.test :as t]
   [datomic.client.api :as d]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.schema :as schema]
   [datomic.ion.rustic.db-utils :as db-utils]
   [datomic.ion.rustic.test-fixtures :as tf]
   [datomic.ion.rustic.utils :as utils]))

(tf/test-setup)

(def rss-post "test/datomic/ion/rustic/fixtures/rss-post.edn")

(def atom-post "test/datomic/ion/rustic/fixtures/atom-post.edn")

(t/deftest find-last-date-tests
  (t/testing "works for rss"
    (t/is (=
           (-> rss-post edn/read poller/find-last-date)
           (instant/read-instant-date "2021-04-23T06:14:01.000-00:00"))))
  (t/testing "works for atom"
    (t/is (=
           (-> atom-post edn/read poller/find-last-date)
           (instant/read-instant-date "2021-11-22T00:00:00.000-00:00")))))

(def mem-notify (atom {}))

(defn reset-notify
  []
  (reset! mem-notify {}))

(defn notified?
  [email]
  (get @mem-notify (keyword email) false))

(defn mock-notify
  [email]
  (swap! mem-notify assoc (keyword email) true))

(t/deftest poll-feed-tests
  (with-redefs [clojure.xml/parse                edn/read
                datomic.ion.rustic.poller/notify mock-notify]
    (let
     [conn  (db-utils/get-connection)
      email "a1"]
      (rustic/register-sub conn email rss-post)
      (t/testing "notifies when no last updated date"
        (reset-notify)
        (t/is (not (notified? email)))
        (poller/poll-feed conn email rss-post)
        (t/is (=
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post))
               (instant/read-instant-date "2021-04-23T06:14:01.000-00:00")))
        (t/is (notified? email)))
      (t/testing "notifies when date was old"
        (let [sub-id (:db/id (schema/find-sub (d/db conn)  email rss-post))
              past-date (instant/read-instant-date "2020-04-23T06:14:01.000-00:00")]
          (d/transact conn {:tx-data [{:db/id sub-id, :sub/last-updated-date past-date}]}))
        (reset-notify)
        (t/is (not (notified? email)))
        (poller/poll-feed conn email rss-post)
        (t/is (notified? email))
        (t/is (=
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post))
               (instant/read-instant-date "2021-04-23T06:14:01.000-00:00"))))
      (t/testing "doesn't notify when up to date"
        (reset-notify)
        (t/is (not (notified? email)))
        (poller/poll-feed conn email rss-post)
        (t/is (not (notified? email)))
        (t/is (=
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post))
               (instant/read-instant-date "2021-04-23T06:14:01.000-00:00")))))))
