(ns clojure-rest.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.utils :as utils]
            [clojure-rest.db-utils :as db-utils]
            [clojure-rest.db :as db]
            [clojure-rest.valid :as valid]
            [clojure-rest.pictures :as pic]
            [clj-http.client :as client]
            [ring.util.response :refer [response]]
            [clojure.set :refer [rename-keys]]))
            
(defn user-from-token
  "return a user from a given token or nil"
  [token]
  (:rel_user (first (jdbc/query @db/db
            ["SELECT rel_user
             FROM tokens
             WHERE access_token = ? AND expire > ?
             LIMIT 1" token (utils/timestamp)]))))
   
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
  [conn email]
  (:id (first (jdbc/query conn
            ["SELECT id
             FROM users
             WHERE email ilike ?
             LIMIT 1" email]))))

(defn ^:private username-in-db?
  "return the id from the username or nil if mail not present"
  [conn username]
  (:id (first (jdbc/query conn
            ["SELECT id
             FROM users
             WHERE username ilike ?
             LIMIT 1" username]))))
             
(defn test-username!
  "Check if the username is already taken and send back a response to the client"
  [username]
    (try 
      (if (username-in-db? @db/db username)
        (utils/make-error 423 {:username username :available false})
        (response {:username username :available true}))
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))
      
(defn test-email!
  "Check if the email is already taken and send back a response to the client"
  [email]
    (try 
      (if (email-in-db? @db/db email)
        (utils/make-error 423 {:email email :available false})
        (response {:email email :available true}))
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))
      
(defn ^:private refresh-token
  "insert a token for a given id and return the token string"
  [conn id-user]
  (let [access-token (db-utils/generate-token-map)]
    (do
      (db-utils/update-or-insert! conn :tokens 
        (assoc access-token :rel_user id-user) ["rel_user = ?" id-user])
      (:access_token access-token))))
      
(defn get-my-profile!
  "fetch an user knowing his id"
  [user-id]
    (try 
      (return-private-profile
        (first 
          (jdbc/query @db/db
            ["SELECT *
             FROM users
             WHERE id = ? 
             LIMIT 1" user-id])))
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))

(defn register!
  "Register a user in database (at least: email / username / picture)"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([user]
    (try 
      (jdbc/with-db-transaction [t-con @db/db]
        (if (and (and (valid/email-address? (:email user)) (not (email-in-db? t-con (:email user))))
                 (and (valid/username? (:username user)) (not (username-in-db? t-con (:username user))))
                 (or (nil? (:gender user)) (valid/gender? (:gender user))))
          (let [{:keys [username email picture gender password]} user
                salt (db-utils/generate-salt) 
                hashedpassword (db-utils/pbkdf2 password salt)
                ret (first 
                      (jdbc/insert! t-con :users
                        {:email (clojure.string/lower-case email)
                         :username username
                         :picture (pic/return-uri picture)
                         :gender gender
                         :salt salt
                         :password hashedpassword}))
                     ]
                (-> ret
                    (assoc :access_token (refresh-token t-con (:id ret))) 
                    return-private-profile))
            (register!)))
          (catch Exception e (utils/make-error 500 "Unable to insert this user in database")))))

(defn update!
  "Update a user in database"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([user id-user & [provider]]
    (try 
      (jdbc/with-db-transaction [t-con @db/db]
        (let [id-mail (email-in-db? t-con (:email user))
              id-username (username-in-db? t-con (:username user))]
          (if (and (or (nil? (:email user)) (and (valid/email-address? (:email user)) 
                                                 (or (not id-mail) (= id-user id-mail))))
                   (or (nil? (:username user)) (and (valid/username? (:username user)) 
                                                    (or (not id-username) (= id-user id-username))))
                   (or (nil? (:gender user)) (valid/gender? (:gender user))))
            (let [{:keys [username email picture gender]} user
                  picture (pic/return-uri picture)
                  ret (first 
                        (jdbc/update! t-con :users 
                            ;we avoid setting variables to null with this reduction
                            (reduce-kv (fn [m k v] (if (nil? v) m (assoc m k v))) {}
                             {:email (clojure.string/lower-case email)
                              :username username
                              :picture picture
                              :gender gender}) ["id = ?" id-user]))]
              (-> (if provider 
                    (assoc user :access_token (refresh-token t-con id-user)) 
                    (dissoc user :access_token))
                  (conj (when picture [:picture picture]))
                  return-private-profile))
            (update!))))
        (catch Exception e (utils/make-error 500 "Unable to insert this user in database")))))
      
(defn login!
  "Log the user and return his informations"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([user]
    (if (and (or (valid/email-address? (:email user))
                 (valid/username? (:email user)))
             (string? (:password user)))
      (let [{:keys [email password]} user]
        (try 
          (jdbc/with-db-transaction [t-con @db/db]
            (let [ret (first
                        (jdbc/query t-con
                          ["SELECT *
                           FROM users
                           WHERE email ilike ? OR username ilike ?
                           LIMIT 1" email email]))
                  hashedpassword (db-utils/pbkdf2 password (:salt ret))]
                (if (= hashedpassword (:password ret))      
                  (-> ret
                      (assoc :access_token (refresh-token t-con (:id ret))) 
                      return-private-profile)
                  (utils/make-error 401 "Wrong credentials."))))
          (catch Exception e (utils/make-error 500 "Unable to log in."))))
      (login!))))
      
(defn logout!
  "remove the user token in database"
  [user-id]
    (try 
      (do
        (jdbc/query @db/db
          ["DELETE FROM tokens
           WHERE rel_user = ?
           RETURNING rel_user" user-id])
        {:body "Success."})
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))
      
(defn delete-profile!
  "remove the user from the database, require user password"
  [user-id user-infos]
  (try 
    (jdbc/with-db-transaction [t-con @db/db]
      (let [ret (first
                  (jdbc/query t-con
                    ["SELECT *
                     FROM users
                     WHERE id = ?
                     LIMIT 1" user-id]))
            hashedpassword (db-utils/pbkdf2 (:password user-infos) (:salt ret))]
          (if (= hashedpassword (:password ret))
            (do
              (jdbc/query t-con
                ["DELETE FROM users
                 WHERE id = ?
                 RETURNING id" user-id])
              {:body "Success."})
            (utils/make-error 500 "Wrong password."))))
    (catch Exception e (utils/make-error 500 "Unable to request the database."))))

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user provider]
    (let [formatted-user (rename-keys user {:name :username})
          id-user (email-in-db? @db/db (:email formatted-user))]
      (if id-user
        (update! formatted-user id-user provider)
        (register! formatted-user))))

(defn auth-google
  "Authenticate a user with google access token"
  [token]
  (try
    (let [req (client/get "https://www.googleapis.com/oauth2/v1/userinfo" 
                {:query-params {"alt" "json" "access_token" token} :as :json-strict})]
      (auth-connect (:body req) "google"))
    (catch Exception e (utils/make-error 409 "Bad Google token"))))
      
(defn auth-facebook
  "Authenticate a user with facebook access token"
  [token]
  (try
    (let [req (client/get "https://graph.facebook.com/v2.6/me" 
                {:query-params {"fields" "name,email,gender,last_name,first_name,picture"
                 "access_token" token} :as :json-strict})
          picture (:url (:data (:picture (:body req))))]
      (auth-connect (assoc (:body req) :picture picture) "facebook"))
    (catch Exception e (utils/make-error 409 "Bad Facebook token"))))
