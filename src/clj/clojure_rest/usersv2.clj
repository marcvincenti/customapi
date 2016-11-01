(ns clojure-rest.usersv2
  (:refer-clojure :exclude [get])
  (:require [ring.util.response :refer [response]]
            [amazonica.aws.dynamodbv2 :as amz]
            [clojure-rest.utils :as utils]
            [clojure-rest.valid :as valid]
            [clj-http.client :as client]))
  
(def ^:private client-opts
  {:access-key "AKIAIKZWOA4I5Y43GDOA"
   :secret-key "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"
   :endpoint   "http://dynamodb.eu-west-1.amazonaws.com"})


(defn ^:private return-user-object
  "return a public user object"
  [user]
  (let [{:keys [id name picture gender token]} user]
    (response {:data {:id id 
                      :name name 
                      :picture picture 
                      :gender gender 
                      :token token}})))
   
(defn get
  "Returns info from a user with given id"
  [id]
  (return-user-object 
    (first (:items 
      (amz/query client-opts
        :table-name "users"
        :limit 1
        :key-conditions {:id {:attribute-value-list [id] 
                              :comparison-operator "EQ"}})))))

(defn ^:private email-in-db?
  "return true if the email is already present in db"
  [email]
    (= 1 (:count 
      (amz/query client-opts
        :table-name "users"
        :limit 1
        :select "COUNT"
        :key-conditions {:id {:attribute-value-list [(sha1 email)] :comparison-operator "EQ"}}))))

(defn register!
  "Register a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)), 
         (string? (:name user)), 
         (string? (:picture user)), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [now (utils/current-iso-8601-date)
        item (assoc user :id (sha1 (:email user)) 
                        :token (utils/generate-token) 
                        :registration now)]
    (try (amz/put-item client-opts 
                :table-name "users" 
                :item item)
            (return-user-object item)
        (catch Exception e (utils/make-error 500 "Registration Failed.")))))


;TODO : Put new variables in databases when the object already exist
(defn update!
  "Update a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)),
         (or (nil? (:picture user)) (string? (:name user))), 
         (or (nil? (:picture user)) (string? (:picture user))), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [now (utils/current-iso-8601-date)
        id (sha1 (:email user))
        item (assoc user :token (utils/generate-token))]
    (try 
      (amz/update-item client-opts 
        :table-name "users" 
        :key {:id id}
        :attribute-updates {:last_connection {:value now :action "PUT"}
                            :token {:value (:token item) :action "PUT"}})
        ;(reduce #(conj %1 (first %2) {:value (second %2) :action "PUT"}) [] item)
       (return-user-object item)
       (catch Exception e (utils/make-error 500 "Update Failed.")))))

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user]
    (if (email-in-db? (:email user))
      (update! user)
      (register! user)))

(defn auth-google
  "Authenticate a user with google access token"
  [token]
  (try
    (let [req (client/get "https://www.googleapis.com/oauth2/v1/userinfo" 
                {:query-params {"alt" "json" "access_token" token} :as :json-strict})]
        (auth-connect (:body req)))
    (catch Exception e (utils/make-error 409 "Invalid Access Token."))))
    
(defn auth-facebook
  "Authenticate a user with facebook access token"
  [token]
  (let [req (client/get "https://graph.facebook.com/v2.6/me" 
              {:query-params {"fields" "name,email,gender,about,relationship_status,interested_in,religion,bio,last_name,birthday,age_range,first_name,hometown,picture,devices,education"
               "access_token" token} :as :json-strict})
        picture (:url (:data (:picture (:body req))))]
    (try (auth-connect (assoc (:body req) :picture picture))
      (catch Exception e (utils/make-error 409 "Invalid Access Token.")))))


