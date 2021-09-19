(ns datomic.ion.rustic
  (:require
   [datomic.client.api :as d]
   [datomic.ion.rustic.db-utils :as db-utils]))

(def get-db db-utils/get-db)

(def get-connection db-utils/get-connection)

(defn get-subs-by-email
  "Returns all subs matching given email."
  [db email pull-expr]
  (d/q '[:find (pull ?e pull-expr)
         :in $ ?email pull-expr
         :where [?e :sub/email ?email]]
       db email pull-expr))

(defn sub-exists
  "Returns all subs matching given email."
  [db email rss]
  (-> (d/q '[:find ?e
             :in $ [?email ?rss]
             :where [?e :sub/email ?email]
             [?e :sub/rss ?rss]]
           db [email rss])
      first
      some?))

(defn register-sub
  "Creates sub for a given email and blog."
  [conn email rss]
  (when-not (sub-exists (d/db conn) email rss)
    (d/transact conn {:tx-data [{:sub/email email, :sub/rss rss}]})))

(defn poll-subs
  "Get last query time & see if there are any updates since then."
  [conn email rss]
  (d/transact conn {:tx-data [{:sub/email email, :sub/rss rss}]}))
