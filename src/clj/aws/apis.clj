(ns aws.apis
  (:require [ring.util.response :refer [response status]]
            [amazonica.core :refer [ex->map]]
            [amazonica.aws.apigateway :as api
              :only [create-rest-api delete-rest-api get-rest-apis]]))

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
  "Get all Rest apis"
  [cred]
  (try
    (response {:projects (map #(update-in % [:created-date] str)
      (get (api/get-rest-apis cred {:region "eu-west-1"}) :items))})
    (catch Exception e (let [err (ex->map e)]
      (status (response (:message err)) (:status-code err))))))

(defn create-project
  "Add a new Rest api"
  [{:keys [access-key secret-key name description version]}]
  (let [creds {:access-key access-key :secret-key secret-key}]
  (try
    (api/create-rest-api creds
      {:name name :description "My api description" :version "1.0.0"})
  (catch Exception e (let [err (ex->map e)]
    (status (response (:message err)) (:status-code err)))))))

(defn delete-project
  "Delete old Rest api"
  [{:keys [access-key secret-key id]}]
  (let [creds {:access-key access-key :secret-key secret-key}]
  (try
    (api/delete-rest-api creds {:rest-api-id id})
    {:success (str "Project " id " deleted.")}
  (catch Exception e (let [err (ex->map e)]
    (status (response (:message err)) (:status-code err)))))))
