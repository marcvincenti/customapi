(ns aws.cognito
  (:require [amazonica.aws.cognitoidentity :as cognito
              :only [create-user-pool]]))

(defn set-authentication
  "Create an aws cognito identity pool for our users"
  [app-name]
  (comment (cognito/create-user-pool 
    {:pool-name app-name})))
