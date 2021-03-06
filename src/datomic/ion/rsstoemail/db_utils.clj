(ns datomic.ion.rsstoemail.db-utils (:require
                                     [clojure.edn :as edn]
                                     [clojure.java.io :as io]
                                     [datomic.client.api :as d]
                                     [datomic.ion.rsstoemail.schema :as schema]
                                     [datomic.ion.rsstoemail.utils :as utils]))

(def database-name "datomic-rustic")

(def get-client
  "Return a shared client."
  (memoize #(if-let [r (io/resource "datomic/ion/rsstoemail/config.edn")]
              (d/client (edn/read-string (slurp r)))
              (throw (RuntimeException. "bad")))))

(defn get-connection
  "Get shared connection."
  []
  (utils/with-retry #(d/connect (get-client) {:db-name database-name})))

(defn get-db
  "Returns current db value from shared connection."
  []
  (d/db (get-connection)))

;; (defn delete-db [] (d/delete-database (get-client) {:db-name database-name}))

(defn load-schemas
  "Load the schemas and create db."
  []
  (let [client (get-client)]
    (d/create-database client {:db-name database-name})
    (let [conn (get-connection)]
      (schema/load-schema conn))))

(defn ensure-schemas-loaded
  "Creates db (if necessary) and schemas with retries."
  []
  (utils/with-retry load-schemas))
