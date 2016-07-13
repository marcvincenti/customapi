(ns clojure-rest.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.utils :as utils]
            [clojure-rest.db-utils :as db-utils]
            [clojure-rest.db :as db]
            [clojure-rest.valid :as valid]
            [clojure-rest.pictures :as pictures]
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
  "return the id from the email or nil if mail not present"
  [email]
  (:id (first (jdbc/query @db/db
            ["SELECT id
             FROM users
             WHERE email = ?
             LIMIT 1" email]))))

(defn register!
  "Register a user in database (at least: email / username / picture)"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([user]
  (if (and (valid/email-address? (:email user))
           (string? (:username user))
           (valid/image-uri? (:picture user))
           (or (nil? (:gender user)) (valid/gender? (:gender user))))
    (let [{:keys [username email picture gender password]} user
          salt (db-utils/generate-salt) 
          hashedpassword (db-utils/pbkdf2 password salt)
          access-token (db-utils/generate-token-map)]
      (try 
        (jdbc/with-db-transaction [t-con @db/db]
          (let [ret (first 
                      (jdbc/insert! t-con :users
                        {:email email
                         :username username
                         :picture picture
                         :gender gender
                         :salt salt
                         :password hashedpassword}))]
            (do
              (jdbc/insert! t-con :tokens 
                (assoc access-token :rel_user (ret :id)))
              (-> ret
                  (assoc :access_token (:access_token access-token))
                  return-private-profile))))
        (catch Exception e (utils/make-error 500 "Unable to insert this user in database"))))
      (register!))))

(defn update!
  "Update a user in database"
  [user & [id-user]]
  {:pre [(valid/email-address? (:email user)),
         (or (nil? (:username user)) (string? (:username user))), 
         (or (nil? (:picture user)) (string? (:picture user))), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [id-user (or id-user 10)                                         ;TODO : replace 10 with the userid  from future wrapper
        {:keys [username email picture gender]} user
        access-token (db-utils/generate-token-map)]
    (try 
      (jdbc/with-db-transaction [t-con @db/db]
        (let [ret (first 
          (jdbc/update! t-con :users 
              ;we avoid setting variables to null with this reduction
              (reduce-kv (fn [m k v] (if (nil? v) m (assoc m k v))) {}
               {:username username
                :picture picture
                :gender gender}) ["email = ?" email]))]
          (do
            (db-utils/update-or-insert! t-con :tokens 
              (assoc access-token :rel_user id-user) ["rel_user = ?" id-user])
            (-> user
                (assoc :access_token (:access_token access-token))
                return-private-profile))))
      (catch Exception e (utils/make-error 500 "Unable to update user in database")))))

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user]
    (let [formatted-user (rename-keys user {:name :username})
          id-user (email-in-db? (:email formatted-user))]
      (if id-user
        (update! formatted-user id-user)
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
