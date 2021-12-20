(ns datomic.ion.rustic.auth
  (:require
   [datomic.ion :as ion]
   [clj-jwt.core  :refer :all]
   [clj-jwt.intdate :refer [intdate->joda-time]]
   [clj-jwt.key   :refer [private-key public-key]]
   [clj-time.core :refer [now plus days before?]]))

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
           :email email
           :exp (plus (now) (days 14))
           :iat (now)}]
    (-> claim jwt (sign :ES256 ec-prv-key) to-str)))

(defn valid-jwt?
  [token]
  (let  [jwt (str->jwt token)
         exp (-> jwt :claims :exp intdate->joda-time)]
    (and
     (verify jwt ec-pub-key)
     (before? (now) exp))))

(defn decode-jwt-claim
  [token]
  (-> token str->jwt :claims))


