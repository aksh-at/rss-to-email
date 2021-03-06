(ns datomic.ion.rsstoemail.poller (:require
                                   [clojure.instant :as instant]
                                   [clojure.xml :as xml]
                                   [datomic.client.api :as d]
                                   [datomic.ion.cast :as cast]
                                   [datomic.ion.rsstoemail.mailer :as mailer]
                                   [datomic.ion.rsstoemail.schema :as schema]
                                   [datomic.ion.rsstoemail.utils :as u]
                                   [clj-http.client :as client]
                                   [clj-time.core :as t]
                                   [clj-time.format :as tf]
                                   [clj-time.coerce :as tc]))

(defn find-tag [x input]
  (filter #(= (% :tag) x) input))

(def rss-formatter (tf/formatters :rfc822))

(defn is-recent-post [last-updated-date post-date]
  (or (nil? last-updated-date) (== -1 (compare last-updated-date post-date))))

(defn rss-post-date [c]
  (->> c
       :content
       (find-tag :pubDate)
       first :content
       first
       (tf/parse rss-formatter)
       tc/to-date))

(defn rss-new-posts [xml-content last-updated-date]
  (->> xml-content
       :content
       first :content
       (find-tag :item)
       (filter #(is-recent-post last-updated-date (rss-post-date %)))))

(defn atom-post-date [c]
  (->> c
       :content
       (find-tag :published)
       first :content
       first
       instant/read-instant-date))

(defn atom-new-posts [xml-content last-updated-date]
  (->> xml-content
       :content
       (find-tag :entry)
       (filter #(is-recent-post last-updated-date (atom-post-date %)))))


(defn get-new-posts [xml-content last-updated-date]
  (case (:tag xml-content)
    :rss (rss-new-posts xml-content last-updated-date)
    :feed (atom-new-posts xml-content last-updated-date)
    (throw (Exception. "Unsupported feed type. Use RSS or Atom XML feed."))))

(defn update-and-notify
  "First notify user by sending email, and then update last-updated-date in DB."
  [conn email feed-url sub-id new-posts]
  (mailer/notify email feed-url new-posts)
  (def cur-date (tc/to-date (t/now)))
  (d/transact conn {:tx-data [{:db/id sub-id, :sub/last-updated-date cur-date}]}))

(defn get-xml [feed-url]
  (try
    (xml/parse feed-url)
    (catch Exception e
      (-> feed-url
          client/get
          :body
          .getBytes
          java.io.ByteArrayInputStream.
          xml/parse))))

(defn poll-feed
  "Poll a URL, update last updated date in DB and send notification if updated."
  [conn [email feed-url]]
  (cast/event {:msg (format "Polling %s %s..." email feed-url)})
  (try
    (let [xml-content (get-xml feed-url)
          db (d/db conn)
          {sub-id :db/id last-updated-date :sub/last-updated-date} (schema/find-sub db email feed-url)
          new-posts (get-new-posts xml-content last-updated-date)]
      (cast/event {:msg (format "Found %d posts for %s" (count new-posts) feed-url)})
      (when (< 0 (count new-posts))
        (update-and-notify conn email feed-url sub-id new-posts)))
    (catch Exception e
      (cast/event {:msg "Caught exception" :ex e}))))
