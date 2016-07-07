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
          :subname "//another.ctcyur2o6hny.eu-west-1.rds.amazonaws.com:5432/postgres"
          }})
      
(defn table-exist?
  "Check if the schema from the database contains the table"
  [table-name profile]
  (-> (jdbc/query profile
          ["SELECT EXISTS(SELECT * FROM information_schema.tables WHERE table_name = ?)" table-name])
      first
      :exists))

(defn ^:private create-user-db [profile]
  (if-not (table-exist? "users" profile)
    (do
      ;(jdbc/query profile ["DROP TYPE IF EXISTS gender; CREATE TYPE gender AS ENUM ('male', 'female'); SELECT 1 as r"])
      (jdbc/db-do-prepared profile
        (ddl/create-table :users
          [:id :serial "PRIMARY KEY"]
          [:email "varchar(32)" "UNIQUE" "NOT NULL"]
          [:username "varchar(32)" "NOT NULL"]
          [:gender "TYPE gender AS ENUM ('male', 'female')"]
          [:picture "VARCHAR(2083)"]
          [:salt "bytea"]
          [:password "VARCHAR(40)"]))))
  (if-not (table-exist? "roles" profile)
    (do
      (jdbc/db-do-prepared profile
        (ddl/create-table :roles
          [:id :serial "PRIMARY KEY"]
          [:name "varchar(16)" "NOT NULL" "UNIQUE"]))
      (jdbc/insert! profile :roles {:name "verified"})
      (jdbc/insert! profile :roles {:name "admin"})))
  (if-not (table-exist? "user_role" profile)
    (jdbc/db-do-prepared profile
      (ddl/create-table :user_role
        [:user :serial "REFERENCES users(id)"]
        [:role :serial "REFERENCES roles(id)"]
        ["PRIMARY KEY (user, role)"])))
  (if-not (table-exist? "tokens" profile)
    (jdbc/db-do-prepared profile
      (ddl/create-table :tokens
        [:user :serial "references users(id)"]
        [:access_token "varchar(36)" "NOT NULL" "UNIQUE"]
        [:expire "int(10)" "NOT NULL" "DEFAULT UNIX_TIMESTAMP()+(24*60*60)" "ON UPDATE UNIX_TIMESTAMP()+(24*60*60)"]))))
            
            
(defn init-db! [profile]
  {:pre [(get allowed-profiles profile)]}
  (->> profile
       (get db-specs)
       (merge {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"})
       pool/make-datasource-spec
       (reset! db)
       create-user-db))

