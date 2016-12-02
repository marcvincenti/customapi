(ns aws.core
  (:require [ring.util.response :refer [response status]]
            [amazonica.core :refer [ex->map]]
            [amazonica.aws.identitymanagement :as iam
              :only [get-group list-users create-user]]))
              
(def aws-path "/serverless/")

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

(defn get-projects
  "Check provided data"
  [cred]
  (let [group "myawesomeapi"]
    (try
      (response {:projects (map #(update-in % [:create-date] str)
          (get (iam/list-users cred {:path-prefix aws-path}) :users))})
      (catch Exception e (let [err (ex->map e)]
        (status (response (:message err)) (:status-code err)))))))

(defn create-project
  "Add a new project"
  [{:keys [access-key secret-key name]}]
  (let [creds {:access-key access-key :secret-key secret-key}]
  (try
    (iam/create-user creds {:user-name name :path aws-path})
  (catch Exception e (let [err (ex->map e)]
    (status (response (:message err)) (:status-code err)))))))
