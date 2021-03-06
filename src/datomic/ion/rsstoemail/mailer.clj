(ns datomic.ion.rsstoemail.mailer
  (:use [hiccup.core])
  (:require
   [ses-mailer.core :as m]
   [datomic.ion.rsstoemail.auth :as auth]
   [datomic.ion.rsstoemail.utils :as u]
   [clojure.string :as str])
  (:import (com.amazonaws.auth DefaultAWSCredentialsProviderChain)))

;; Constants.

(def from-email "\"RSS To Email\" <mailer@rssto.email>")

(defn get-manage-link
  [email]
  (format "https://rssto.email/manage?token=%s" (auth/create-jwt email)))

(defn get-register-link
  [email feed-url]
  (format "https://rssto.email/register?feed=%s&token=%s" feed-url (auth/create-jwt email)))

;; Helpers.

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

(defn post-html [post]
  (let [title  (get-post-title post)
        description (get-post-description post)
        link (get-post-link post)]
    [:div
     [:a {:href link} [:h1 title]]
     [:p description]]))

(defn format-notify-body [feed-url new-posts manage-link]
  (html [:html
         [:body
          [:div (seq  (mapv post-html new-posts))]
          [:div {:style  {:display "flex" :justify-content "center"}}
           [:a {:href manage-link :style {:color "rgb(150,150,150) !important"
                                          :text-decoration "underline !important"
                                          :font-size "0.8em !important"}}
            "Unsubscribe"]]]]))

(defn notify [email feed-url new-posts]
  (let [manage-link (get-manage-link email)
        subject-line (format-notify-subject-line feed-url new-posts)
        body (format-notify-body feed-url new-posts manage-link)]
    (send-email email subject-line body)))

;; Send subscription confirmation.

(defn format-sub-conf-subject-line [feed-url]
  (format "Confirm subscription to %s"
          (get-homepage-from-feed feed-url)))

(defn make-button
  [link label]
  [:a {:style {:background-color "rgb(245, 167, 66)"
               :border "none"
               :border-radius "4px"
               :box-sizing "border-box"
               :cursor "pointer"
               :display "inline-block"
               :height "42px"
               :line-height "19px !important"
               :padding "10px 20px"
               :text-align "center"
               :color "rgb(30, 30, 30)"
               :text-decoration "none"}
       :href link}
   label])

(defn format-sub-conf-body [feed-url link]
  (let [homepage (get-homepage-from-feed feed-url)]
    (html [:html
           [:body
            [:div (format "Confirm your subscription to %s:" homepage)]
            [:div {:style  {:display "flex" :justify-content "center"}}
             (make-button link "Confirm subscription")]]])))

(defn send-sub-confirmation [email feed-url]
  (let [link (get-register-link email feed-url)
        subject-line (format-sub-conf-subject-line feed-url)
        body (format-sub-conf-body feed-url link)]
    (send-email email subject-line body)))

;; Send manage confirmation.

(defn format-manage-conf-subject-line [] "Manage your subscriptions")

(defn format-manage-conf-body [num-subs link]
  (html [:html
         [:body
          [:div
           (format "You have %d active subscriptions. Click here to manage:" num-subs)]
          [:div {:style  {:display "flex" :justify-content "center"}}
           (make-button link "Manage subscriptions")]]]))

(defn send-manage-confirmation [email num-subs]
  (let [link (get-manage-link email)
        subject-line (format-manage-conf-subject-line)
        body (format-manage-conf-body num-subs link)]
    (send-email email subject-line body)))
