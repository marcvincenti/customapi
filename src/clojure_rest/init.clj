(ns clojure-rest.init
  (:require [aws.dynamoDB :as dynamodb]
            [aws.s3 :as s3]
            [aws.core :as aws]))
            
(defn init! []
  "Initializing aws"
  (do
    ;prefix name of all data initialized in the app.
    (aws/set-app-name "rapid-framework")
  
    ;initializing S3
    (s3/set-bucket "app-test")
      
    ;initializing DynamoDB
    (dynamodb/set-db 
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
