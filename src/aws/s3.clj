(ns aws.s3
  (:require [amazonica.aws.s3 :as s3 
              :only [does-bucket-exist create-bucket list-buckets]]))

(defn set-bucket
  "Create if it doesn't exist yet a bucket"
  [bucket-name app-name]
  (let [bucket-final-name (str app-name "-" bucket-name)]
    (if-not (s3/does-bucket-exist bucket-final-name) 
      (s3/create-bucket bucket-final-name)
      (when (empty? (filter #(= (get % :name) bucket-final-name) (s3/list-buckets)))
        (throw (Exception. (str "[WARNING] The bucket name : " bucket-final-name 
          ", is already taken by someone else, please rename your bucket")))))))
