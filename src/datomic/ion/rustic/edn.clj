;; source: ion-starter

(ns datomic.ion.rustic.edn
  (:refer-clojure :exclude (read))
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :as pp]))

(defn read
  [input-stream]
  (some-> input-stream io/reader (java.io.PushbackReader.) edn/read))

(defn write-str
  "Full pretty print of x to a string."
  ^String [x]
  (binding [*print-length* nil
            *print-level* nil]
    (with-out-str (pp/pprint x))))

(defn input-stream
  "Open an inputstream on edn representation of x.
Not efficient."
  [x]
  (-> x write-str (.getBytes "UTF-8") java.io.ByteArrayInputStream.))

(defn write-file
  [value filename]
  (with-open [w (clojure.java.io/writer filename)]
    (binding [*print-length* false ;; don't cap print length
              *out* w]
      (pr value))))
