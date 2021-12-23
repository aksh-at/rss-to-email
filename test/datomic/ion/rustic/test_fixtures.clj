(ns datomic.ion.rustic.test-fixtures
  (:require
   [clojure.test :as t]
   [datomic.dev-local :as dl]
   [datomic.ion.rustic.db-utils :as db-utils]))

(defn with-db [f]
  (db-utils/load-schemas)
  (f))

(defn test-setup []
;; this is so scary
  (dl/divert-system {:system "datomic-4" :storage-dir :mem})
  (t/use-fixtures :once with-db))
