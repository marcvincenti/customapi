(ns aws.aws
  (:require [aws.dynamoDB :as dynamodb]
            [aws.data :as data]
            [amazonica.aws.s3 :as s3 :only [does-bucket-exist create-bucket]]))

;bucket-name (prefix will be added automatically)
(def s3-bucket
  "app-test")

;dynamo-db tables (prefix will be added automatically)
(def dynamo-db-onjs
  "Our objects definitions"
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
              :picture {:type "File"}}}
  :qsdds {:keys {
              :id {:type "Index"}}
           :data {
              :name {:type "String"}
              :password {:type "String"}
              :salt {:type "Binary"}
              :creation-date {:type "Integer"}
              :last-connection {:type "Integer"}
              :picture {:type "String"}}}})

(defn set-app-name
  "Change global app-name"
  [new-name]
  (reset! data/app-name (clojure.string/lower-case new-name)))
            
(defn ^:private add-prefix
  "Add prefix to a string if app-name is set"
  [str-name]
  (str @data/app-name "-" str-name))
            
(defn init! []
  "Initializing aws"
  (do
    ;prefix name of all data initialized in the app.
    (set-app-name "rapid-framework")
  
    ;initializing S3
    (if s3-bucket
      (let [bucket-name (add-prefix s3-bucket)]
        (when-not (s3/does-bucket-exist bucket-name) 
          (s3/create-bucket bucket-name))))
      
    ;initializing DynamoDB
    (if dynamo-db-onjs
      (dynamodb/update-db dynamo-db-onjs))))
