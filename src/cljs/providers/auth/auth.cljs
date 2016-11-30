(ns providers.auth
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [app.state :refer [app-state]]
            [providers.cookies :as cookies]))

(defn login
  "Log a user with AWS credentials"
  [loading error]
  (reset! loading true)
  (go (let [response (<! (http/post "/api/login" {:form-params (get @app-state :creds)}))]
    (if (:success response)
      ;login success
      (swap! app-state assoc :projects (get-in response [:body :projects])
                             :page :projects
                             :connected true)
      ;login failed
      (do (swap! app-state assoc-in [:creds :secret-key] "")
        (reset! error true)))
    (reset! loading false))))

(defn logout
  "Log the user out"
  []
  (swap! app-state dissoc :creds :connected :projects))
