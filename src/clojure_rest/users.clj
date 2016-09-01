(ns clojure-rest.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.utils :as utils]
            [clojure-rest.db-utils :as db-utils]
            [clojure-rest.db :as db]
            [clojure-rest.data-verification :as verif]
            [clojure-rest.pictures :as pic]
            [clj-http.client :as client]
            [ring.util.response :refer [response]]
            [clojure.set :refer [rename-keys]]))
            
(defn user-from-token
  "return a user from a given token or nil"
  [token]
  (:rel_user (first (jdbc/query
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
  [user & [options]]
    (response (select-keys user [:username :picture :gender :email :access_token options])))
    
(defn ^:private email-in-db?
  "return the id from the email or nil if mail not present"
  [email]
  (:id (first (jdbc/query
            ["SELECT id
             FROM users
             WHERE email ilike ?
             LIMIT 1" email]))))

(defn ^:private username-in-db?
  "return the id from the username or nil if mail not present"
  [username]
  (:id (first (jdbc/query
            ["SELECT id
             FROM users
             WHERE username ilike ?
             LIMIT 1" username]))))
             
(defn test-username!
  "Check if the username is already taken and send back a response to the client"
  [username]
  (let [errors (verif/check {:data username :function verif/xusername? :dataname "username" :required true})]
    (if errors (utils/make-error 400 errors)
      (try 
        (if (username-in-db? username)
          (utils/make-error 423 {:username username :available false})
          (response {:username username :available true}))
        (catch Exception e (utils/make-error 500 "Unable to request the database."))))))
        
(defn xabsent-username?
  "Return string error if the username is already in db."
  [uname]
  (if (empty? (jdbc/query
            ["SELECT id
             FROM users
             WHERE username ilike ?
             LIMIT 1" uname]))
    nil
    "This username is already used."))
      
(defn test-email!
  "Check if the email is already taken and send back a response to the client"
  [email]
  (let [errors (verif/check {:data email :function verif/xemail-address? :dataname "email" :required true})]
    (if errors (utils/make-error 400 errors)
      (try 
        (if (email-in-db? email)
          (utils/make-error 423 {:email email :available false})
          (response {:email email :available true}))
        (catch Exception e (utils/make-error 500 "Unable to request the database."))))))
        
(defn xabsent-email?
  "Return string error if the email is already in db."
  [email]
  (if (empty? (jdbc/query
            ["SELECT id
             FROM users
             WHERE email ilike ?
             LIMIT 1" email]))
    nil
    "This email address is already used."))
      
(defn ^:private refresh-token
  "insert a token for a given id and return the token string"
  [id-user]
  (let [access-token (db-utils/generate-token-map)]
    (do
      (db-utils/update-or-insert! :tokens 
        (assoc access-token :rel_user id-user) ["rel_user = ?" id-user])
      (:access_token access-token))))
      
(defn get-my-profile!
  "fetch an user knowing his id"
  [user-id]
    (try 
      (return-private-profile
        (first 
          (jdbc/query
            ["SELECT *
             FROM users
             WHERE id = ? 
             LIMIT 1" user-id])))
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))

(defn register!
  "Register a user in database (at least: email / username / picture)"
  [{:keys [username email picture gender password] :as user}]
  (let [errors (verif/check {:data email :function [:and verif/xemail-address? xabsent-email?] :dataname "email" :required true}
                            {:data username :function [:and verif/xusername? xabsent-username?] :dataname "username" :required true}
                            {:data password :function verif/xpassword?}
                            {:data picture :function verif/xpic?}
                            {:data gender :function verif/xgender?})]
    (if (-> errors empty? not) (utils/make-error 400 errors)
      (try 
        (if (and (and (verif/email-address? email) (not (email-in-db? email)))
                 (and (verif/username? username) (not (username-in-db? username)))
                 (or (nil? gender) (verif/gender? gender)))
          (let [salt (db-utils/generate-salt) 
                hashedpassword (db-utils/pbkdf2 password salt)
                ret (first 
                      (jdbc/insert! :users
                        {:email (clojure.string/lower-case email)
                         :username username
                         :picture (pic/return-uri picture)
                         :gender gender
                         :salt salt
                         :password hashedpassword}))
                     ]
                (-> ret
                    (assoc :access_token (refresh-token (:id ret))) 
                    return-private-profile)))
            (catch Exception e (utils/make-error 500 "Unable to insert this user in database"))))))

(defn update!
  "Update a user in database"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([{:keys [username email picture gender oldpassword newpassword] :as user} id-user]
    (try 
      (let [id-mail (email-in-db? email)
            id-username (username-in-db? username)]
        (if (and (or (nil? email) (and (verif/email-address? email) 
                                               (or (not id-mail) (= id-user id-mail))))
                 (or (nil? username) (and (verif/username? username) 
                                                  (or (not id-username) (= id-user id-username))))
                 (or (nil? gender) (verif/gender? gender))
                 (or (nil? newpassword) (and (string? newpassword) (not-empty newpassword))))
          (let [picture (pic/return-uri picture)
                user-pass (first
                            (jdbc/query
                              ["SELECT password, salt
                               FROM users
                               WHERE id = ?
                               LIMIT 1" id-user]))
                password (if (= (db-utils/pbkdf2 oldpassword (:salt user-pass)) (:password user-pass))
                              (db-utils/pbkdf2 newpassword (:salt user-pass))
                              nil)
                ret (first 
                      (jdbc/update! :users 
                          ;we avoid setting variables to null with this reduction
                          (reduce-kv (fn [m k v] (if (nil? v) m (assoc m k v))) {}
                           {:email (clojure.string/lower-case email)
                            :username username
                            :picture picture
                            :password password
                            :gender gender}) ["id = ?" id-user]))]
            (-> (dissoc user :access_token)
                (conj (when picture [:picture picture]) (when newpassword [:passwordUpdate (if password "true" "false")]))
                (return-private-profile :passwordUpdate)))
          (update!)))
        (catch Exception e (utils/make-error 500 "Unable to insert this user in database")))))
      
(defn login!
  "Log the user and return his informations"
  ([] (utils/make-error 400 "Required parameters are missing or are invalid."))
  ([{:keys [email password] :as user}]
    (if (and (or (verif/email-address? email)
                 (verif/username? email))
             (string? password))
      (try 
        (let [ret (first
                    (jdbc/query
                      ["SELECT *
                       FROM users
                       WHERE email ilike ? OR username ilike ?
                       LIMIT 1" email email]))
              hashedpassword (db-utils/pbkdf2 password (:salt ret))]
            (if (= hashedpassword (:password ret))      
              (-> ret
                  (assoc :access_token (refresh-token (:id ret))) 
                  return-private-profile)
              (utils/make-error 401 "Wrong credentials.")))
        (catch Exception e (utils/make-error 500 "Unable to log in.")))
      (login!))))
      
(defn logout!
  "remove the user token in database"
  [user-id]
    (try 
      (do
        (jdbc/query
          ["DELETE FROM tokens
           WHERE rel_user = ?
           RETURNING rel_user" user-id])
        {:body "Success."})
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))
      
(defn delete-profile!
  "remove the user from the database, require user password"
  [user-id user-infos]
  (try 
    (let [ret (first
                (jdbc/query
                  ["SELECT *
                   FROM users
                   WHERE id = ?
                   LIMIT 1" user-id]))
          hashedpassword (db-utils/pbkdf2 (:password user-infos) (:salt ret))]
        (if (= hashedpassword (:password ret))
          (do
            (jdbc/query
              ["DELETE FROM users
               WHERE id = ?
               RETURNING id" user-id])
            {:body "Success."})
          (utils/make-error 500 "Wrong password.")))
    (catch Exception e (utils/make-error 500 "Unable to request the database."))))

(defn ^:private auth-connect
  "Register the user in database or update his profile"
  [user]
    (try 
      (let [in-db-user (first (jdbc/query
                          ["SELECT *
                           FROM users
                           WHERE email ilike ?
                           LIMIT 1" (:email user)]))]
        (if in-db-user
          (return-private-profile (assoc in-db-user :access_token (refresh-token (:id in-db-user))))
          (register! (rename-keys user {:name :username}))))
      (catch Exception e (utils/make-error 500 "Unable to request the database."))))

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
