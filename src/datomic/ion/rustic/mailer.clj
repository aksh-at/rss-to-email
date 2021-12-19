(ns datomic.ion.rustic.mailer
  (:require
   [ses-mailer.core :as m]
   [datomic.ion.rustic.utils :as u]
   [clojure.string :as str])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

(def from-email "\"RSS To Email\" <no-reply@rssto.email>")

(def client-opts {:provider (DefaultAWSCredentialsProviderChain.) :region :us-east-2})

(defn send-email
  [to-email subject-line body]
  (println (format "Sending email to %s with subject %s..." to-email subject-line))
  (m/send-email client-opts
                from-email
                to-email
                subject-line
                {:html-body body}))

;; Update notifications.

(defn find-tag [x input]
  (filter #(= (% :tag) x) input))

(defn get-homepage-from-feed [feed-url]
  (let [tokens (str/split feed-url #"/")
        t1 (first tokens)]
    (if (str/starts-with? t1 "http")
      (get tokens 2)
      t1)))

(defn format-notify-subject-line [feed-url new-posts]
  (format "%d new updates from %s"
          (count new-posts)
          (get-homepage-from-feed feed-url)))

(defn get-post-title [post] (->> post :content (find-tag :title) first :content first))

(defn get-post-description [post]
  (let [description (->> post :content (find-tag :description) first :content first)
        content (->> post :content (find-tag :content) first :content first)]
    (or description content "")))

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

(defn format-notify-body [feed-url new-posts]
  (let [formatted-posts (mapv format-post new-posts)
        joined-posts (str/join "\n" formatted-posts)]
    (format "<html>%s</html>" joined-posts)))

(defn notify [email feed-url new-posts]
  (let [subject-line (format-notify-subject-line feed-url new-posts)
        body (format-notify-body feed-url new-posts)]
    (send-email email subject-line body)))

;; Send subscription confirmation.

(defn format-sub-conf-subject-line [feed-url]
  (format "Confirm subscription to %s"
          (get-homepage-from-feed feed-url)))

; TODO: figure out a better way to format HTML templates & embed CSS

(defn format-sub-conf-body [feed-url link]
  (let [homepage (get-homepage-from-feed feed-url)]
    (str/join "\n"
              ["<html>"
               (format "Confirm your subscription to %s:" homepage)
               "<div style=\"display: flex; justify-content: center; \">"
               (format "<a style=\"background-color: rgb(245, 167, 66); border: none; border-radius: 4px; box-sizing: border-box; cursor: pointer; display: inline-block; height: 42; line-height: 20px !important; padding: 10px 20px; text-align: center; color: rgb(0, 0, 0); text-decoration: none;\" class=\"button\" href=\"%s\">Confirm subscription</a>" link)
               "</div>"
               "</html>"])))

(defn send-sub-confirmation [email feed-url link]
  (let [subject-line (format-sub-conf-subject-line feed-url)
        body (format-sub-conf-body feed-url link)]
    (send-email email subject-line body)))

;; Send manage confirmation.
