(ns datomic.ion.rsstoemail.schema
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

(defn load-schema
  [conn]
  (let [db (d/db conn)]
    (if (data-loaded? db)
      :already-loaded
      (let [xact #(d/transact conn {:tx-data %})]
        (xact subscription-schema)
        ;; (xact [sample-sub])
        :loaded))))

;; Helper functions for queries.

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

(defn sub-exists?
  [db email feed-url]
  (some? (find-sub db email feed-url)))

(defn get-all-subs
  [db]
  (d/q
   '[:find ?email ?sub
     :where
     [?x :sub/email ?email]
     [?x :sub/feed-url ?sub]] db))

(defn get-subs-by-email
  [db email pull-expr]
  (d/q '[:find (pull ?e pull-expr)
         :in $ ?email pull-expr
         :where [?e :sub/email ?email]]
       db email pull-expr))

(defn reset-last-updated
  [conn email feed-url]
  (let [db (d/db conn)
        {sub-id :db/id last-updated-date :sub/last-updated-date} (find-sub db email feed-url)]
    (d/transact conn
                {:tx-data
                 [[:db/retract sub-id :sub/last-updated-date last-updated-date]]})))

(defn remove-sub
  [conn email feed-url]
  (let [db (d/db conn)
        {sub-id :db/id} (find-sub db email feed-url)]
    (d/transact conn
                {:tx-data
                 [[:db/retractEntity sub-id]]})))
