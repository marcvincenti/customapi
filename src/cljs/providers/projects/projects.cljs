(ns providers.projects
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [app.state :refer [app-state]]))

(defn get-projects
  "Log a user with AWS credentials"
  [loading]
  (reset! loading true)
  (go (let [response (<! (http/get "/api/projects"
                      {:query-params (get @app-state :creds)}))]
    (if (:success response)
      (swap! app-state assoc :projects (get-in response [:body :projects]))
      (.log js/console (str "Error : " (get response :body))))
    (reset! loading false))))

(defn create-project
  "Instanciate a new project"
  [p-name p-descr]
  (go (let [response (<! (http/post "/api/projects"
                      {:query-params (get @app-state :creds)
                       :form-params {:name @p-name :description @p-descr}}))]
    (reset! p-name "")
    (reset! p-descr "")
    (if (:success response)
      (.log js/console (str "Success : " (get response :body)))
      (.log js/console (str "Error : " (get response :body)))))))

(defn delete-project
  "Delete an old project"
  [p]
  (go (let [response (<! (http/delete "/api/projects"
                      {:query-params (get @app-state :creds)
                       :form-params (select-keys p [:id])}))]
    (if (:success response)
      (.log js/console (str "Success : " (get response :body)))
      (.log js/console (str "Error : " (get response :body)))))))
