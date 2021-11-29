(ns datomic.ion.rustic.poller-test
  (:require
   [clojure.instant :as instant]
   [clojure.test :as t]
   [datomic.client.api :as d]
   [clj-time.core :as time]
   [clj-time.coerce :as tc]
   [datomic.ion.rustic :as rustic]
   [datomic.ion.rustic.edn :as edn]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.schema :as schema]
   [datomic.ion.rustic.db-utils :as db-utils]
   [datomic.ion.rustic.test-fixtures :as tf]
   [datomic.ion.rustic.utils :as u]))

(tf/test-setup)

(def rss-post "test/datomic/ion/rustic/fixtures/rss-post.edn")

(def atom-post "test/datomic/ion/rustic/fixtures/atom-post.edn")

(t/deftest new-post-tests
  (t/testing "works for rss"
    (t/testing "when last-updated is nil"
      (def filtered (-> rss-post edn/read (poller/get-new-posts nil)))
      (t/is (= (count filtered) 2)))
    (t/testing "when last-updated is not nil"
      (def filtered (-> rss-post edn/read (poller/get-new-posts (instant/read-instant-date "2021-04-20T00:00:00.000-00:00"))))
      (t/is (= (count filtered) 1))))
  (t/testing "works for atom"
    (t/testing "when last-updated is nil"
      (def filtered (-> atom-post edn/read (poller/get-new-posts nil)))
      (t/is (= (count filtered) 15)))
    (t/testing "when last-updated is not nil"
      (def filtered (-> atom-post edn/read (poller/get-new-posts (instant/read-instant-date "2021-04-20T00:00:00.000-00:00"))))
      (t/is (= (count filtered) 8)))))

(def mem-notify (atom {}))

(defn reset-notify
  []
  (reset! mem-notify {}))

(defn notified?
  [email]
  (get @mem-notify (keyword email) false))

(defn mock-notify
  [email _]
  (swap! mem-notify assoc (keyword email) true))

(defn close-to-now? [start-time]
  (<
   (time/in-millis (time/interval (tc/from-date start-time) (time/now))) 1000))

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
        (t/is (close-to-now?
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post))))
        (t/is (notified? email)))
      (t/testing "notifies when date was old"
        (let [sub-id (:db/id (schema/find-sub (d/db conn)  email rss-post))
              past-date (instant/read-instant-date "2020-04-23T06:14:01.000-00:00")]
          (d/transact conn {:tx-data [{:db/id sub-id, :sub/last-updated-date past-date}]}))
        (reset-notify)
        (t/is (not (notified? email)))
        (poller/poll-feed conn email rss-post)
        (t/is (notified? email))
        (t/is (close-to-now?
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post)))))
      (t/testing "doesn't notify when up to date"
        (reset-notify)
        (t/is (not (notified? email)))
        (poller/poll-feed conn email rss-post)
        (t/is (not (notified? email)))
        (t/is (close-to-now?
               (:sub/last-updated-date (schema/find-sub (d/db conn)  email rss-post))))))))
