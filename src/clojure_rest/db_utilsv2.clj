(ns clojure-rest.db-utilsv2
	(:require [crypto.random :as crypto :only [bytes]]
            [amazonica.aws.dynamodbv2 :as ddb :only [create-table list-tables]]
            [clojure-rest.utils :as utils])
  (import java.security.SecureRandom
          javax.crypto.SecretKeyFactory
          javax.crypto.spec.PBEKeySpec))

(def ^:private cred 
  {:endpoint (str "http://dynamodb." (System/getenv "AWS_REGION") ".amazonaws.com")})

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
     
     
;DynamoDB functions

(def ^:private default-values
  "Some default value to provide to dynamodb"
  {:read-capacity-units 2
   :write-capacity-units 2})
  

(defn ^:private toDynamoDBType
  "Transform a custom type (string) to the corresponding dynamodb type"
  [customType]
    (case customType
        ("Binary") "B"
        ("Index" "Date" "Integer" "Float" "Double") "N"
        ("File" "String" "Char") "S"))
     
(defn init-objects
  "Create tables creation scripts to let dynamoDB store our objects"
  [objs]
    (reduce 
      (fn [arg1 arg2] 
        (let [obj-keys (:keys (second arg2))
              table-name (name (first arg2))
              key-schema 
                (let [hash-var [{:attribute-name (-> obj-keys first first name)
                                 :key-type "HASH"}]
                    range-var (:order-by (-> obj-keys first second))]
                  (if range-var 
                    (conj hash-var {:attribute-name (name range-var)
                                    :key-type "RANGE"})
                              hash-var))
              attribute-definitions 
                (into [] (for [[k v] obj-keys] 
                  {:attribute-name (name k) 
                   :attribute-type (toDynamoDBType (:type v))}))
              provisioned-throughput 
                (let [{:keys [read-capacity-units write-capacity-units] 
                       :or {read-capacity-units (:read-capacity-units default-values)
                            write-capacity-units (:write-capacity-units default-values)}} 
                      (:provisioned-throughput (-> obj-keys first second))]
                  {:read-capacity-units read-capacity-units
                   :write-capacity-units write-capacity-units})
              global-secondary-indexes 
                  (reduce (fn [lst index] 
                    (let [key-schema (let [hash-var [{:attribute-name (-> index first name)
                                                      :key-type "HASH"}]
                                           range-var (:order-by (second index))]
                                        (if range-var 
                                          (conj hash-var {:attribute-name (name range-var)
                                                          :key-type "RANGE"})
                                          hash-var))
                          provisioned-throughput (let [{:keys [read-capacity-units write-capacity-units] 
                                                        :or {read-capacity-units (:read-capacity-units default-values)
                                                             write-capacity-units (:write-capacity-units default-values)}} 
                                                        (:provisioned-throughput (second index))]
                                                    {:read-capacity-units read-capacity-units
                                                     :write-capacity-units write-capacity-units})]
                      (conj lst {:index-name (str "index_" (-> index first name))
                                 :key-schema key-schema
                                 :projection {:projection-type "ALL"}
                                 :provisioned-throughput provisioned-throughput}))) 
                        [] (rest obj-keys))]
        (conj arg1 (let [tablemap {:table-name table-name
                                   :key-schema key-schema
                                   :attribute-definitions attribute-definitions
                                   :provisioned-throughput provisioned-throughput}]
                    (if-not (empty? global-secondary-indexes)
                      (assoc tablemap :global-secondary-indexes global-secondary-indexes)
                      tablemap)))))
      [] objs))
        
(defn create-tables
  "Create tables if they doesn't already exist
   args is a list of tables with amazonica dynamoDB2 format"
  [& args]
    (let [tables (:table-names (ddb/list-tables cred))]
      (reduce #(if-not (some #{(:table-name %2)} %1)
                (do
                  (ddb/create-table cred %2)
                  (conj %1 (:table-name %2)))
                %1)
        tables args)))
