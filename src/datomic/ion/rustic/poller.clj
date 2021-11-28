(ns datomic.ion.rustic.poller (:require
                               [clojure.instant :as instant]
                               [clojure.xml :as xml]
                               [datomic.client.api :as d]
                               [datomic.ion.rustic.schema :as schema]
                               [clj-time.format :as tf]
                               [clj-time.coerce :as tc]))

(defn find-tag [x input]
  (filter #(= (% :tag) x) input))

(def rss-formatter (tf/formatters :rfc822))

(defn find-rss-last-date [xml-content]
  (->> xml-content
       :content
       first :content
       (find-tag :item)
       first :content
       (find-tag :pubDate)
       first :content
       first
       (tf/parse rss-formatter)
       tc/to-date))

(defn find-atom-last-date [xml-content]
  (->> xml-content
       :content
       (find-tag :entry)
       first :content
       (find-tag :published)
       first :content
       first
       instant/read-instant-date))

(defn find-last-date [xml-content]
  (case (:tag xml-content)
    :rss (find-rss-last-date xml-content)
    :feed (find-atom-last-date xml-content)
    (throw (Exception. "Unsupported feed type. Use RSS or Atom XML feed."))))

(defn update-and-notify
  "First notify user by sending email, and then update last-updated-date in DB."
  [conn sub-id new-date]
  (d/transact conn {:tx-data [{:db/id sub-id, :sub/last-updated-date new-date}]}))

(defn poll-feed
  "Poll a URL, update last updated date in DB and send notification if updated."
  [conn email feed-url]
  (let [xml-content (xml/parse feed-url)
        db (d/db conn)
        {sub-id :db/id last-updated-date :sub/last-updated-date} (schema/find-sub db email feed-url)
        new-updated-date (find-last-date xml-content)]
    (println sub-id last-updated-date new-updated-date)
    (when (or (nil? last-updated-date) (== -1 (compare last-updated-date new-updated-date)))
      (update-and-notify conn sub-id new-updated-date))))
