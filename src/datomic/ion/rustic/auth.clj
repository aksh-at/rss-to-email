(ns datomic.ion.rustic.auth
  (:require
   [datomic.ion :as ion]
   [clj-jwt.core  :refer :all]
   [clj-jwt.key   :refer [private-key public-key]]
   [clj-time.core :refer [now plus days]]))

(. java.security.Security addProvider (org.bouncycastle.jce.provider.BouncyCastleProvider.))

(def ec-prv-key
  (-> {:path "/datomic-shared/jwt-key/"}
      ion/get-params
      (get "private")
      char-array
      private-key))

(def ec-pub-key
  (-> {:path "/datomic-shared/jwt-key/"}
      ion/get-params
      (get "public")
      char-array
      public-key))

(defn create-jwt
  [email]
  (let
   [claim {:iss "rustic"
           :sub email
           :exp (plus (now) (days 1))
           :iat (now)}]
    (-> claim jwt (sign :ES256 ec-prv-key) to-str)))

(defn verify-jwt
  [token]
  (-> token str->jwt (verify ec-pub-key)))
