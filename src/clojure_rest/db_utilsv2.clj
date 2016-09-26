(ns clojure-rest.db-utilsv2
	(:require [crypto.random :as crypto :only [bytes]]
            [amazonica.aws.dynamodbv2 :as ddb :only [create-table list-tables]]
            [clojure-rest.utils :as utils])
  (import java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

(def ^:private cred 
  {:endpoint (str "http://dynamodb." (System/getenv "AWS_ENDPOINT") ".amazonaws.com")})

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
        
(defn create-tables
  "Create tables if they doesn't already exist"
  [& args]
    (let [tables (:table-names (ddb/list-tables cred))]
      (reduce #(if-not (some #{(:table-name %2)} %1)
                (do
                  (ddb/create-table cred %2)
                  (conj %1 (:table-name %2)))
                %1)
        tables args)))
