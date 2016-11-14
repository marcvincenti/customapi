(ns aws.core
  (:require [ring.util.response :refer [response status]]
            [amazonica.aws.s3 :as s3 :only [list-buckets]]))

(defn list-regions
  "Return the list of available regions in aws"
  []
  (response
    {:data [{:name "US East (N. Virginia)" :value "us-east-1"}
            {:name "US West (Oregon)" :value	"us-west-2"}
            {:name "Asia Pacific (Seoul)" :value	"ap-northeast-2"}
            {:name "Asia Pacific (Tokyo)" :value	"ap-northeast-1"}
            {:name "EU (Frankfurt)" :value	"eu-central-1"}
            {:name "EU (Ireland)" :value	"eu-west-1"}]}))

(defn login
  "Check provided data"
  [cred]
    (try
      (s3/list-buckets cred)
      (response "Ok :)")
      (catch Exception e (status (response "Invalid credentials.") 403))))
