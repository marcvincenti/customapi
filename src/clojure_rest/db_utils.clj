(ns clojure-rest.db-utils
	(:require [crypto.random :as crypto]
            [clojure.java.jdbc :as jdbc])
  (import java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

(defn generate-token
  "Return a new token access"
  []
  (str (java.util.UUID/randomUUID)))
  
(defn generate-salt
  "Return a new random salt"
  []
  (crypto/bytes 64))
      
(defn pbkdf2
  "Get a hash for the given string and optional salt"
  [x salt]
  (let [k (PBEKeySpec. (.toCharArray x) (.getBytes salt) 1000 192)
        f (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA1")]
    (format "%x"
      (java.math.BigInteger. (.getEncoded (.generateSecret f k))))))
      
(defn update-or-insert!
  "Updates columns or inserts a new row in the specified table"
  [db table row where-clause]
  (jdbc/with-db-transaction [t-con db]
    (let [result (jdbc/update! t-con table row where-clause)]
      (if (zero? (first result))
        (jdbc/insert! t-con table row)
        result))))
