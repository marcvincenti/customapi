(ns aws.apis
  (:require [ring.util.response :refer [response status]]
            [amazonica.core :refer [ex->map]]
            [amazonica.aws.apigateway :as api
              :only [create-rest-api delete-rest-api get-rest-apis]]))

(defn get-projects
  "Get all Rest apis"
  [{:keys [access-key secret-key]}]
  (let [creds {:access-key access-key :secret-key secret-key}]
  (try
    (response {:projects (map #(update-in % [:created-date] str)
      (get (api/get-rest-apis creds {:l ""}) :items))})
    (catch Exception e (let [err (ex->map e)]
      (status (response (:message err)) (:status-code err)))))))

(defn create-project
  "Add a new Rest api"
  [{:keys [access-key secret-key name description version]}]
  (let [creds {:access-key access-key :secret-key secret-key}]
  (try
    (api/create-rest-api creds (into {:name name}
      (when-not (empty? description) {:description description})))
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
