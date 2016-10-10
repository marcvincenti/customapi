(ns clojure-rest.db
  (:require [clojure-rest.db-utilsv2 :refer [update-db]]
            [amazonica.aws.s3 :as s3 :only [does-bucket-exist]]))
  
(def bucket "clojure-api-users-v2")
  
(defn ^:private aws-setup []
  (do
    ;initializing S3
    (when-not (s3/does-bucket-exist bucket) 
      (s3/create-bucket bucket))
    ;initializing DynamoDB
    (update-db
      {:users {:keys {
                :id {:type "Index" 
                     :order-by :creation-date 
                     :provisioned-throughput {:read-capacity-units 5}}
                :email {:type "String"}}
               :data {
                :name {:type "String"}
                :password {:type "String"}
                :salt {:type "Binary"}
                :creation-date {:type "Integer"}
                :last-connection {:type "Integer"}
                :picture {:type "String"}}}})))
            
(defn init! []
  "Initializing aws"
  (do
  
    ;initializing S3
    (when-not (s3/does-bucket-exist bucket) 
      (s3/create-bucket bucket))
      
    ;initializing DynamoDB
    (update-db
      {:users {:keys {
            :id {:type "Index" 
                 :order-by :creation-date 
                 :provisioned-throughput {:read-capacity-units 5}}
            :email {:type "String"}}
           :data {
            :name {:type "String"}
            :password {:type "String"}
            :salt {:type "Binary"}
            :creation-date {:type "Integer"}
            :last-connection {:type "Integer"}
            :picture {:type "String"}}}})))
