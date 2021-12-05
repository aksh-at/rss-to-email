(ns datomic.ion.rustic.mailer
  (:require
   [ses-mailer.core :as m]
   [datomic.ion.rustic.utils :as u]
   [clojure.string :as str])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(defn find-tag [x input]
  (filter #(= (% :tag) x) input))

(def from-email "no-reply@rssto.email")

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

(defn format-post [post]
  (let [title (->> post :content (find-tag :title) first :content first)
        description (->> post :content (find-tag :description) first :content first)]
    (format "<div><h1>%s</h1><p>%s</p></div>" title description)))

(defn format-body [feed-url new-posts]
  (let [formatted-posts (mapv format-post new-posts)
        joined-posts (str/join "\n" formatted-posts)]
    (format "<html>%s</html>" joined-posts)))

(defn notify [email feed-url new-posts]
  (let [subject-line (format-subject-line feed-url new-posts)
        body (format-body feed-url new-posts)]
    (m/send-email client-opts
                  from-email
                  email
                  subject-line
                  {:html-body body})))
