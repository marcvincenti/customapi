(ns aws.s3
  (:require [amazonica.aws.s3 :as s3 
              :only [does-bucket-exist create-bucket list-buckets]]))

(defn set-bucket
  "Create the bucket if it does not exist yet
   Throw an exeption if it does exist and you are not the owner"
  [app-name bucket-name]
  (let [bucket-final-name (str app-name "-" bucket-name)]
    (if-not (s3/does-bucket-exist bucket-final-name) 
      (do
		(println (str "Create bucket \"" bucket-final-name "\"."))
		(s3/create-bucket bucket-final-name))
      (when (empty? (filter #(= (get % :name) bucket-final-name) (s3/list-buckets)))
        (throw (Exception. (str "[WARNING] The bucket name : " bucket-final-name 
          ", is already taken by someone else, please rename your bucket")))))))
