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
  (first (jdbc/query @db/db
            ["SELECT *
             FROM tokens
             WHERE access_token = ? AND expire > ?
             LIMIT 1" token (utils/timestamp)])))
   
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

(defn ^:private username-available?
  "return true if not used"
  [conn username]
  (empty? (jdbc/query conn
            ["SELECT id
             FROM users
             WHERE username ilike ?
             LIMIT 1" username])))
             
(defn test-username!
  "Check if the username is already taken and send back a response to the client"
  [username]
    (try 
      (if (username-available? @db/db username)
        (response {:username username :available true})
        (utils/make-error 423 {:username username :available false}))
      (catch Exception e (utils/make-error 500 "Unable to request the database"))))
      
(defn test-email!
  "Check if the email is already taken and send back a response to the client"
  [email]
    (try 
      (if (email-in-db? @db/db email)
        (utils/make-error 423 {:email email :available false})
        (response {:email email :available true}))
      (catch Exception e (utils/make-error 500 "Unable to request the database"))))
      
(defn ^:private refresh-token
  "insert a token for a given id and return the token string"
  [conn id-user]
  (let [access-token (db-utils/generate-token-map)]
    (do
      (db-utils/update-or-insert! conn :tokens 
        (assoc access-token :rel_user id-user) ["rel_user = ?" id-user])
      (:access_token access-token))))

(defn register!
  "Register a user in database (at least: email / username / picture)"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([user]
    (try 
      (jdbc/with-db-transaction [t-con @db/db]
        (if (and (and (valid/email-address? (:email user)) (not (email-in-db? t-con (:email user))))
                 (and (valid/username? (:username user)) (username-available? t-con (:username user)))
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
  [user & [id-user]]
  {:pre [(valid/email-address? (:email user)),
         (or (nil? (:username user)) (string? (:username user))), 
         (or (nil? (:picture user)) (string? (:picture user))), 
         (or (nil? (:gender user)) (valid/gender? (:gender user)))]}
  (let [{:keys [username email picture gender]} user]
    (try 
      (jdbc/with-db-transaction [t-con @db/db]
        (let [ret (first 
          (jdbc/update! t-con :users 
              ;we avoid setting variables to null with this reduction
              (reduce-kv (fn [m k v] (if (nil? v) m (assoc m k v))) {}
               {:username username
                :picture (pic/return-uri picture)
                :gender gender}) ["email ilike ?" email]))]
          (return-private-profile 
            (if id-user 
              (assoc user :access_token (refresh-token t-con id-user)) 
              user))))
      (catch Exception e (utils/make-error 500 "Unable to update user in database")))))
      
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

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user]
    (let [formatted-user (rename-keys user {:name :username})
          id-user (email-in-db? @db/db (:email formatted-user))]
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
