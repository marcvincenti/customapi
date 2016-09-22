(ns clojure-rest.db-utils
	(:require [crypto.random :as crypto :only [bytes]]
            [clojure-rest.utils :as utils]
            [clojure.java.jdbc :as jdbc :only [query update! insert! db-do-prepared]]
            [java-jdbc.ddl :as ddl :only [create-table]])
  (import java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

(defn generate-token-map
  "Return a new token access and an expiration unix time in a map"
  []
  {:access_token (utils/uid)
   :expire (+ 
              (utils/timestamp)
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
  ([conn table-name]
    (table-exist? conn table-name "public"))
  ([conn table-name table-schema]
    (-> (jdbc/query conn
            ["SELECT EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)" 
              table-schema table-name])
        first
        :exists)))
        
(defn create-table
  "Create this table if it doesn't already exist"
  [conn table-name table-schema & args]
    (if-not (table-exist? conn table-name table-schema)
      (jdbc/db-do-prepared conn
        (apply ddl/create-table (str table-schema "." table-name) args))))
