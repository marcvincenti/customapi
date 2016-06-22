(ns clojure-rest.users
  (:refer-clojure :exclude [get])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.utils :as utils]
            [clojure-rest.db :as db]
            [clojure-rest.valid :as valid]
            [clj-http.client :as client]
            [ring.util.response :refer [response]]))

(defn ^:private return-user-object
  "return a public user object"
  [user]
  (let [{:keys [id name picture gender token]} user]
    (response  {:id id 
                :name name 
                :picture picture 
                :gender gender 
                :token token})))
   
(defn get
  "Returns info from a user with given id"
  [id]
  (return-user-object 
    (first (jdbc/query @db/db
                ["SELECT *
                 FROM users
                 WHERE id = ?
                 LIMIT 1" id]))))
                 
(defn get-id
  "Return id of the user who have this token"
  [token]
  (:id (first 
          (jdbc/query @db/db
            ["SELECT id
             FROM users
             WHERE access_token = ?
             LIMIT 1" token]))))

(defn ^:private email-in-db?
  "return true if the email is already present in db"
  [email]
  (= 1 (:count 
    (first (jdbc/query @db/db
            ["SELECT id
             FROM users
             WHERE email = ?
             LIMIT 1" email])))))

(defn register!
  "Register a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)), 
         (string? (:name user)), 
         (string? (:profile_picture user)), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [{:keys [name email profile_picture gender]} user
        now (utils/current-iso-8601-date)
        salt (utils/generate-salt) 
        access_token (utils/generate-token)]
    (try (return-user-object 
            (jdbc/insert! @db/db :users 
              {:email email 
               :name name 
               :profile_picture profile_picture 
               :gender gender 
               :salt salt 
               :access_token access_token}))
        (catch Exception e (utils/make-error 500 "Registration Failed.")))))


(defn update!
  "Update a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)),
         (or (nil? (:name user)) (string? (:name user))), 
         (or (nil? (:profile_picture user)) (string? (:profile_picture user))), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [{:keys [name email profile_picture gender]} user
        now (utils/current-iso-8601-date)
        access_token (utils/generate-token)]
    (try 
      (return-user-object 
            (jdbc/update! @db/db :users 
              {:username name 
               :profile_picture profile_picture 
               :gender gender 
               :access_token access_token } ["email = ?" email]))
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
              {:query-params {"fields" "name,email,gender,last_name,first_name,picture"
               "access_token" token} :as :json-strict})
        picture (:url (:data (:picture (:body req))))]
    (try (auth-connect (assoc (:body req) :picture picture))
      (catch Exception e (utils/make-error 409 "Invalid Access Token.")))))

(defn logout
  "logout the logged user depending on his id and return success message"
  [id]
  (jdbc/update! @db/db :users {:access_token nil} ["id = ?" id])
  {:success "You're unlogged."})
