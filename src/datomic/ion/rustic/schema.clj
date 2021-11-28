(ns datomic.ion.rustic.schema
  (:require
   [datomic.client.api :as d]))

(def subscription-schema
  [{:db/ident :sub/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :sub/feed-url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :sub/email+feed-url
    :db/valueType :db.type/tuple
    :db/tupleAttrs [:sub/email :sub/feed-url]
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}
   {:db/ident :sub/last-updated-date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}])

(def sample-sub
  {:sub/email "akshatb42@gmail.com"
   :sub/feed-url "https://aksh-at.github.io/index.xml"
   :sub/last-updated-date (new java.util.Date) })

(defn- has-ident?
  [db ident]
  (contains? (d/pull db {:eid ident :selector [:db/ident]})
             :db/ident))

(defn- data-loaded?
  [db]
  (has-ident? db :sub/email))

(defn find-sub
  [db email feed-url]
  (-> (d/q '[:find (pull ?e [:db/id :sub/email :sub/feed-url :sub/last-updated-date])
             :in $ [?email ?feed-url]
             :where
             [?e :sub/email ?email]
             [?e :sub/feed-url ?feed-url]]
           db [email feed-url])
      first
      first))

(defn load-schema
  [conn]
  (let [db (d/db conn)]
    (if (data-loaded? db)
      :already-loaded
      (let [xact #(d/transact conn {:tx-data %})]
        (xact subscription-schema)
        ;; (xact [sample-sub])
        :loaded))))
