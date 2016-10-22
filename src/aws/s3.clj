(ns aws.s3
  (:require [amazonica.aws.s3 :as s3 :only [does-bucket-exist create-bucket]]))

(defn set-bucket
  "Create if it doesn't exist yet a bucket"
  [bucket-name app-name]
  (let [bucket-final-name (str app-name "-" bucket-name)]
    (when-not (s3/does-bucket-exist bucket-final-name) 
      (s3/create-bucket bucket-final-name))))
