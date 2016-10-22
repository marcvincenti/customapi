(ns clojure-rest.init
  (:require [aws.dynamoDB :as dynamodb]
            [aws.s3 :as s3]))
            
(def app-name "rapid-framework")            
            
(defn init! []
  "Initializing aws"
  (do
  
    ;initializing S3
    (s3/set-bucket app-name "app-test")
      
    ;initializing DynamoDB
    (dynamodb/set-db app-name
      {:users {:keys {
                  :id {:type "Index" 
                       :order-by :creation-date 
                       :provisioned-throughput {:read-capacity-units 1}}
                  :email {:type "String"
                          :order-by :last-connection }
                  :name  {:type "String"}}
               :data {
                  :password {:type "String"}
                  :salt {:type "Binary"}
                  :creation-date {:type "Integer"}
                  :last-connection {:type "Integer"}
                  :picture {:type "File"}}}})))
