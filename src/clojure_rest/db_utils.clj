(ns clojure-rest.db-utils
	(:require [crypto.random :as crypto]
            [clojure.java.jdbc :as jdbc])
  (import java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

(defn generate-token-map
  "Return a new token access and an expiration unix time in a map"
  []
  {:access_token (str (java.util.UUID/randomUUID))
   :expire (+ 
              (quot (System/currentTimeMillis) 1000)
              (* 60 60 24 7))})
  
(defn generate-salt
  "Return a new random salt"
  []
  (crypto/bytes 64))
      
(defn pbkdf2
  "Get a hash for the given string and salt"
  [x salt]
  (if (nil? x) nil
    (let [k (PBEKeySpec. (.toCharArray x) salt 1000 192)
          f (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
      (format "%x"
        (java.math.BigInteger. (.getEncoded (.generateSecret f k)))))))
      
(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [t-con table row where-clause]
    (let [result (jdbc/update! t-con table row where-clause)]
      (if (zero? (first result))
        (jdbc/insert! t-con table row)
        result)))
        
      
(defn table-exist?
  "Check if the schema from the database contains the table"
  [table-name db-specs]
  (-> (jdbc/query db-specs
          ["SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = ?)" table-name])
      first
      :exists))
