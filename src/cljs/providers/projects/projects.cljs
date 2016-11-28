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
  (go (let [response (<! (http/get "/api/projects" {:query-params (get @app-state :creds)}))]
    (if (:success response)
      (swap! app-state assoc :projects (get-in response [:body :projects]))
      (.log js/console (str "Error : " (get response :body))))
    (reset! loading false))))
