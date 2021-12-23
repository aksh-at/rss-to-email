(ns datomic.ion.rustic
  (:require
   [datomic.client.api :as d]
   [datomic.ion.cast :as cast]
   [datomic.ion.rustic.mailer :as mailer]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.schema :as schema]
   [datomic.ion.rustic.db-utils :as db-utils]))

(cast/initialize-redirect :stderr)

(def get-db db-utils/get-db)

(def get-connection db-utils/get-connection)

;; Manage existing subs

(defn get-subs-by-email
  [db email pull-expr]
  (d/q '[:find (pull ?e pull-expr)
         :in $ ?email pull-expr
         :where [?e :sub/email ?email]]
       db email pull-expr))

(defn request-manage
  [conn email]
  (cast/event {:msg "Requesting manage." :email email})
  (def num-subs (count (get-subs-by-email (d/db conn) email [:sub/feed-url])))
  (mailer/send-manage-confirmation email num-subs))

;; Register new sub

(defn request-sub
  [conn email feed-url]
  (cast/event {:msg "Requesting sub." :email email :feed-url feed-url})
  (if (schema/sub-exists? (d/db conn) email feed-url)
    {:status :EXISTS}
    (do
      (mailer/send-sub-confirmation email feed-url)
      {:status :OK})))

(defn register-sub
  [conn email feed-url]
  (cast/event {:msg "Registering sub." :email email :feed-url feed-url})
  (if (schema/sub-exists? (d/db conn) email feed-url)
    {:status :EXISTS}
    (do
      (d/transact conn {:tx-data [{:sub/email email, :sub/feed-url feed-url}]})
      {:status :OK})))

;; Poller

(defn poll-all
  [conn]
  (cast/event {:msg "Starting polling."})
  (let [db (d/db conn)
        all-subs (schema/get-all-subs db)]
    (map #(poller/poll-feed conn %) all-subs)))
