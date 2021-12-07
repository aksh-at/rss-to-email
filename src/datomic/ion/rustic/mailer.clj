(ns datomic.ion.rustic.mailer
  (:require
   [ses-mailer.core :as m]
   [datomic.ion.rustic.utils :as u]
   [clojure.string :as str])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(defn find-tag [x input]
  (filter #(= (% :tag) x) input))

(def from-email "\"RSS To Email\" <no-reply@rssto.email>")

(def client-opts {:provider (DefaultAWSCredentialsProviderChain.) :region :us-east-2})

(defn get-homepage-from-feed [feed-url]
  (let [tokens (str/split feed-url #"/")
        t1 (first tokens)]
    (if (str/starts-with? t1 "http")
      (get tokens 2)
      t1)))

(defn format-subject-line [feed-url new-posts]
  (format "%d new updates from %s"
          (count new-posts)
          (get-homepage-from-feed feed-url)))

(defn get-post-title [post] (->> post :content (find-tag :title) first :content first))

(defn get-post-description [post]
  (let [description (->> post :content (find-tag :description) first :content first)
        content (->> post :content (find-tag :content) first :content first)]
    (or description content)))

(defn get-post-link [post]
  (let [block (->> post :content (find-tag :link) first)
        content (->> block :content first)
        attr (->> block :attrs :href)]
    (or content attr)))

(defn format-post [post]
  (let [title  (get-post-title post)
        description (get-post-description post)
        link (get-post-link post)]
    (format
     "<div><a href=%s><h1>%s</h1></a><p>%s <a href=%s>...</a> </p></div>"
     link
     title
     description
     link)))

(defn format-body [feed-url new-posts]
  (let [formatted-posts (mapv format-post new-posts)
        joined-posts (str/join "\n" formatted-posts)]
    (format "<html>%s</html>" joined-posts)))

(defn notify [email feed-url new-posts]
  (let [subject-line (format-subject-line feed-url new-posts)
        body (format-body feed-url new-posts)]
    (println (format "Sending email to %s with subject %s..." email subject-line))
    (m/send-email client-opts
                  from-email
                  email
                  subject-line
                  {:html-body body})))
