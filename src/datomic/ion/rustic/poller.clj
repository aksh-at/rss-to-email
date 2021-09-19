(ns datomic.ion.rustic.poller (:require
                               [clojure.instant :as instant]
                               [clojure.xml :as xml]
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

(defn poll
  "Poll a URL, update last-checked in DB and send notification if updated."
  [email feed-url]
  (let [xml-content (xml/parse feed-url)
        last-date ()])
  )
