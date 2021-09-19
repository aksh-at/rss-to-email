(ns datomic.ion.rustic.schema
  (:require
   [datomic.client.api :as d]))

(def subscription-schema
  [{:db/ident :sub/email
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :sub/rss
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :sub/email+rss
    :db/valueType :db.type/tuple
    :db/tupleAttrs [:sub/email :sub/rss]
    :db/cardinality :db.cardinality/one
    :db/unique :db.unique/identity}
   {:db/ident :sub/last-checked
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}])

(def sample-sub
  {:sub/email "akshatb42@gmail.com"
   :sub/rss "https://aksh-at.github.io/index.xml"
   :sub/last-checked (new java.util.Date) })

(defn- has-ident?
  [db ident]
  (contains? (d/pull db {:eid ident :selector [:db/ident]})
             :db/ident))

(defn- data-loaded?
  [db]
  (has-ident? db :inv/sku))

(defn load-schema
  [conn]
  (let [db (d/db conn)]
    (if (data-loaded? db)
      :already-loaded
      (let [xact #(d/transact conn {:tx-data %})]
        (xact subscription-schema)
        ;; (xact [sample-sub])
        :loaded))))
