(ns clojure-rest.aws
  (:require [clojure-rest.app :as app]
			[clojure-rest.dynamoDB :as dynamodb]
            [amazonica.aws.s3 :as s3 :only [does-bucket-exist]]))

(def bucket-name 
  (str "rapid-" (clojure.string/lower-case app/app-name)))
            
(defn init! []
  "Initializing aws"
  (do
    ;initializing S3
    (when-not (s3/does-bucket-exist bucket-name) 
      (s3/create-bucket bucket-name))
      
    ;initializing DynamoDB
    (dynamodb/update-db app/objects)))
