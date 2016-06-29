(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [clojure.java.jdbc :as jdbc]
            [jdbc.pool.c3p0 :as pool]
            [java-jdbc.ddl :as ddl]))

(def db (atom nil))
(def current-profile (atom nil))

(def ^:private allowed-profiles #{:prod :test})

(defn set-profile! [profile]
  {:pre [(get allowed-profiles profile)]}
  (reset! current-profile profile))

(def ^:private db-specs
  {:prod {:user (System/getenv "DATABASE_USER")
          :password (System/getenv "DATABASE_PASSWORD")
          :subname (System/getenv "DATABASE_SUBNAME")
          }
   :test {:user "root"
          :password "toortoor"
          :subname "//test.ctcyur2o6hny.eu-west-1.rds.amazonaws.com:3306/testting"
          }})

(defn ^:private create-user-db [profile]
  (if (empty? (jdbc/query profile ["SHOW TABLES LIKE 'users'"]))
    (jdbc/db-do-prepared profile
      (ddl/create-table :users
        [:id "BIGINT" "NOT NULL" "AUTO_INCREMENT" "PRIMARY KEY"]
        [:email "varchar(32)" "UNIQUE" "NOT NULL"]
        [:username "varchar(32)" "NOT NULL"]
        [:gender "ENUM('male','female')"]
        [:picture "VARCHAR(2083)"]
        [:salt "BINARY(64)"]
        [:password "VARCHAR(40)"])))
  (if (empty? (jdbc/query profile ["SHOW TABLES LIKE 'roles'"]))
    (do
      (jdbc/db-do-prepared profile
        (ddl/create-table :roles
          [:id "MEDIUMINT" "NOT NULL" "AUTO_INCREMENT" "PRIMARY KEY"]
          [:name "varchar(16)" "NOT NULL" "UNIQUE"]))
      (jdbc/insert! profile :roles {:name "verified"})
      (jdbc/insert! profile :roles {:name "admin"})))
  (if (empty? (jdbc/query profile ["SHOW TABLES LIKE 'user_role'"]))
    (jdbc/db-do-prepared profile
      (ddl/create-table :user_role
        [:user "BIGINT" "NOT NULL"]
        [:role "MEDIUMINT" "NOT NULL"]
        ["PRIMARY KEY (user, role)"]
        ["FOREIGN KEY (user) REFERENCES users(id)" "ON DELETE CASCADE"]
        ["FOREIGN KEY (role) REFERENCES roles(id)" "ON DELETE CASCADE"])))
  (if (empty? (jdbc/query profile ["SHOW TABLES LIKE 'tokens'"]))
    (jdbc/db-do-prepared profile
      (ddl/create-table :tokens
        [:user "BIGINT" "NOT NULL" "PRIMARY KEY"]
        [:access_token "varchar(36)" "NOT NULL" "UNIQUE"]
        ["FOREIGN KEY (user) REFERENCES users(id)" "ON DELETE CASCADE"]))))
            
            
(defn init-db! [profile]
  {:pre [(get allowed-profiles profile)]}
  (->> profile
       (get db-specs)
       (merge {:classname "com.mysql.jdbc.Driver"
               :subprotocol "mysql"})
       pool/make-datasource-spec
       (reset! db)
       create-user-db))

