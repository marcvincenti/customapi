(ns clojure-rest.files
  (:require [amazonica.aws.s3 :refer [get-object, put-object]]
            [clojure.java.io :refer [input-stream]]
            [clojure-rest.utils :as utils])
  (:import java.io.ByteArrayOutputStream))

(def ^:private cred 
  {:access-key "AKIAIKZWOA4I5Y43GDOA"
   :secret-key "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"
   :endpoint   "eu-west-1"})
  
(defn post-pic
  "Uploads a picture to S3, return the new filename"
  [file]
  (let [{:keys [tempfile]} file
        out (ByteArrayOutputStream.)
        new-filename (str (java.util.UUID/randomUUID) ".png")]
      (utils/make-thumbnail (input-stream tempfile) out 500)
      (try 
        (put-object cred
            :bucket-name "wonespictures"
            :key new-filename
            :input-stream (input-stream (.toByteArray out)))
         new-filename
        (catch Exception e nil))))

