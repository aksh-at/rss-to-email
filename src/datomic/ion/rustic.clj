(ns datomic.ion.rustic
  (:require
   [datomic.client.api :as d]
   [datomic.ion.cast :as cast]
   [datomic.ion.rustic.poller :as poller]
   [datomic.ion.rustic.schema :as schema]
   [datomic.ion.rustic.db-utils :as db-utils]))

(cast/initialize-redirect :stderr)

(def get-db db-utils/get-db)

(def get-connection db-utils/get-connection)

(defn get-subs-by-email
  "Returns all subs matching given email."
  [db email pull-expr]
  (d/q '[:find (pull ?e pull-expr)
         :in $ ?email pull-expr
         :where [?e :sub/email ?email]]
       db email pull-expr))

(defn sub-exists?
  [db email feed-url]
  (some? (schema/find-sub db email feed-url)))

(defn register-sub
  "Creates sub for a given email and blog."
  [conn email feed-url]
  (when-not (sub-exists? (d/db conn) email feed-url)
    (d/transact conn {:tx-data [{:sub/email email, :sub/feed-url feed-url}]})))

(defn get-all-subs
  [db]
  (d/q
   '[:find ?email ?sub
     :where
     [?x :sub/email ?email]
     [?x :sub/feed-url ?sub]] db))

(defn poll-all
  [conn]
  (cast/event {:msg "Starting polling."})
  (let [db (d/db conn)
        all-subs (get-all-subs db)]
    (map #(poller/poll-feed conn %) all-subs)))
