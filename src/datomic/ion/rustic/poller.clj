(ns datomic.ion.rustic.poller (:require
                               [clojure.instant :as instant]
                               [clojure.xml :as xml]
                               [clj-time.corece :as tc]
                               [clj-time.format :as tf]))

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
  (try
    (find-rss-last-date xml-content)
    (catch Exception _ (find-atom-last-date xml-content))))
