(ns clojure-rest.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.utils :as utils]
            [clojure-rest.db-utils :as db-utils]
            [clojure-rest.db :as db]
            [clojure-rest.valid :as valid]
            [clj-http.client :as client]
            [ring.util.response :refer [response]]
            [clojure.set :refer [rename-keys]]))
   
(defn ^:private return-public-profile
  "return a public user profile"
  [user]
    (response (select-keys user [:username :picture :gender])))
    
(defn ^:private return-private-profile
  "return a private user profile"
  [user]
    (response (select-keys user [:username :picture :gender :email :access_token])))
    
(defn ^:private email-in-db?
  "return true if the email is already present in db"
  [email]
  (not-empty (jdbc/query @db/db
            ["SELECT id
             FROM users
             WHERE email = ?
             LIMIT 1" email])))

(defn register!
  "Register a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)), 
         (string? (:username user)), 
         (string? (:picture user)), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [{:keys [username email picture gender password]} user
        salt (db-utils/generate-salt) 
        hashedpassword (db-utils/pbkdf2 password salt)
        access_token (db-utils/generate-token)]
    (try 
      (-> (jdbc/insert! @db/db :users 
            {:email email 
             :username username 
             :picture picture 
             :gender gender 
             :salt salt
             :password hashedpassword})
            first
          return-private-profile)
      (catch Exception e (utils/make-error 500 "Unable to insert user in database")))))

(defn update!
  "Update a user in database"
  [user]
  {:pre [(valid/email-address? (:email user)),
         (or (nil? (:username user)) (string? (:username user))), 
         (or (nil? (:picture user)) (string? (:picture user))), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [{:keys [username email picture gender]} user
        access_token (db-utils/generate-token)]
    (try 
      (do (jdbc/update! @db/db :users 
            ;we avoid setting variables to null with this reduction
            (reduce-kv (fn [m k v] (if (nil? v) m (assoc m k v))) {}
             {:username username
              :picture picture
              :gender gender}) ["email = ?" email])
          (return-private-profile user))
      (catch Exception e (utils/make-error 500 "Unable to update user in database")))))

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user]
    (let [formatted-user (rename-keys user {:name :username})]
      (if (email-in-db? (:email formatted-user))
        (update! formatted-user)
        (register! formatted-user))))

(defn auth-google
  "Authenticate a user with google access token"
  [token]
  (try
    (let [req (client/get "https://www.googleapis.com/oauth2/v1/userinfo" 
                {:query-params {"alt" "json" "access_token" token} :as :json-strict})]
      (auth-connect (:body req)))
    (catch Exception e (utils/make-error 409 "Bad Google token"))))
      
(defn auth-facebook
  "Authenticate a user with facebook access token"
  [token]
  (try
    (let [req (client/get "https://graph.facebook.com/v2.6/me" 
                {:query-params {"fields" "name,email,gender,last_name,first_name,picture"
                 "access_token" token} :as :json-strict})
          picture (:url (:data (:picture (:body req))))]
      (auth-connect (assoc (:body req) :picture picture)))
    (catch Exception e (utils/make-error 409 "Bad Facebook token"))))
