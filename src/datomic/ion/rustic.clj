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
  [conn email]
  (let [db (d/db conn)
        all-subs  (schema/get-subs-by-email db email [:sub/email :sub/feed-url])]
    {:subs all-subs :email email}))

(defn request-manage
  [conn email]
  (cast/event {:msg "Requesting manage." :email email})
  (def num-subs (count (get-subs-by-email (d/db conn) email [:sub/feed-url])))
  (mailer/send-manage-confirmation email num-subs))

(defn unsubscribe
  [conn email feed-url]
  (cast/event {:msg "Unsubscribing." :email email :feed-url feed-url})
  (schema/remove-sub conn email feed-url))

;; Register new sub

(defn request-sub
  [conn email feed-url]
  (cast/event {:msg "Requesting sub." :email email :feed-url feed-url})
  (if (schema/sub-exists? (d/db conn) email feed-url)
    :exists
    (do
      (mailer/send-sub-confirmation email feed-url)
      :ok)))

(defn register-sub
  [conn email feed-url]
  (cast/event {:msg "Registering sub." :email email :feed-url feed-url})
  (if (schema/sub-exists? (d/db conn) email feed-url)
    :exists
    (do
      (d/transact conn {:tx-data [{:sub/email email, :sub/feed-url feed-url}]})
      :ok)))

;; Poller

(defn poll-all
  [conn]
  (cast/event {:msg "Starting polling."})
  (let [db (d/db conn)
        all-subs (schema/get-all-subs db)]
    (map #(poller/poll-feed conn %) all-subs)))
