(ns datomic.ion.rustic
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [datomic.client.api :as d]
   [datomic.ion.rustic.schema :as schema]
   [datomic.ion.rustic.utils :as utils]))

(def database-name "datomic-rustic")

(def get-client
  "Return a shared client."
  (memoize #(if-let [r (io/resource "datomic/ion/rustic/config.edn")]
              (d/client (edn/read-string (slurp r)))
              (throw (RuntimeException. "bad")))))

(defn get-connection
  "Get shared connection."
  []
  (utils/with-retry #(d/connect (get-client) {:db-name database-name})))

(defn load-schemas
  "Load the schemas and create db."
  []
  (let [client (get-client)]
    (d/create-database client {:db-name database-name})
    (let [conn (get-connection)]
      (schema/load-schema conn))))

(defn get-db
  "Returns current db value from shared connection."
  []
  (d/db (get-connection)))

(defn get-subs-by-email
  "Returns all subs matching given email."
  [db email pull-expr]
  (d/q '[:find (pull ?e pull-expr)
         :in $ ?email pull-expr
         :where [?e :sub/email ?email]]
       db email pull-expr))

(defn register-sub
  "Creates sub for a given email and blog."
  [conn email rss]
  (d/transact conn {:tx-data [{:sub/email email, :sub/rss rss}]}))

(defn ensure-schemas-loaded
  "Creates db (if necessary) and schemas with retries."
  []
  (utils/with-retry load-schemas))
