(ns aws.cognito
  (:require [amazonica.aws.cognitoidentity :as cognito
              :only [create-identity-pool list-identity-pools]]))

(defn set-authentication
  "Create an aws cognito identity pool for our users
  if it doesn't exist yet"
  [app-name]
  ;TODO: remove the max-results limit and use pagination instead
  (when (empty? (filter #(= (get % :identity-pool-name) app-name)
                  (:identity-pools (cognito/list-identity-pools {:max-results 60}))))
    (do
      (println (str "Create identity pool \"" app-name "\"."))
      (cognito/create-identity-pool
          {:allow-unauthenticated-identities false
           :identity-pool-name app-name}))))
