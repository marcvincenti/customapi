(ns providers.auth
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as r]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [app.state :refer [app-state]]))

;auth-guard

;unauth-guard

(defn login
  "Log a user with AWS credentials"
  []
  (go (let [response (<! (http/post "/api/login" {:form-params (get @app-state :creds)}))]
    (if (:success response)
      (swap! app-state assoc :projects (get-in response [:body :projects])
                             :connected true)
      (swap! app-state assoc-in [:creds :secret-key] "")))))

(defn logout
  "Log the user out"
  []
  (swap! app-state dissoc :creds :connected :projects))
