(ns datomic.ion.rsstoemail.lambdas
  (:require
   [clojure.data.json :as json]
   [datomic.ion.rsstoemail :as rsstoemail]
   [datomic.ion.rsstoemail.edn :as edn]))


(defn get-subs-by-email
  [{:keys [input]}]
  (-> (rsstoemail/get-db)
      (rsstoemail/get-subs-by-email (json/read-str input)
                                    [:sub/email :sub/feed-url])
      edn/write-str))



(defn poll-all
  [{:keys [input]}]
  (-> (rsstoemail/get-connection)
      rsstoemail/poll-all
      doall
      count
      str))
