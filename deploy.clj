#!/usr/bin/env bb

(require
  '[clojure.java.shell :refer [sh]]
  '[clojure.string :refer [split-lines includes? split replace]])


(def push-output
  (sh "clojure" "-A:ion-dev" "{:op :push}"))

(println push-output)

;; super hacky
(def command-string  (->> push-output
                          :out
                          split-lines
                          (filter #(includes? % "-A:ion-dev"))
                          first))

(println command-string)

(def deploy-command (-> command-string
                        (split #"'")
                        (get 1)
                        (replace #"\\" "")))

(println deploy-command)

(def deploy-output (sh "clojure" "-A:ion-dev" deploy-command))

(println deploy-output)
