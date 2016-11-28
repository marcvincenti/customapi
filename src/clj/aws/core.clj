(ns aws.core
  (:require [ring.util.response :refer [response status]]
            [amazonica.core :refer [ex->map]]
            [amazonica.aws.identitymanagement :as iam
              :only [get-group create-group create-user]]))

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
                        (get (iam/get-group cred {:group-name group}) :users))})
      (catch Exception e (let [err (ex->map e)]
        (if (= "NoSuchEntity" (:error-code err))
          (try
            (do
              (iam/create-group cred {:group-name group})
              (response {:projects []}))
            (catch Exception e (let [err (ex->map e)]
              (status (response (:message err)) (:status-code err)))))
          (status (response (:message err)) (:status-code err))))))))

(defn add-project
  "Add a new project"
  [cred goup user]
  (try
    (iam/create-user cred {:user-name user :path "/user/myawesomeapi"})
  (catch Exception e (let [err (ex->map e)]
    (status (response (:message err)) (:status-code err))))))
