(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.db-utils :refer [table-exist?]]
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

(defn ^:private create-user-db [profile]
  (if-not (table-exist? "users" profile)
    (jdbc/db-do-prepared profile
      (ddl/create-table :users
        [:id :serial "PRIMARY KEY"]
        [:email "varchar(32)" "UNIQUE" "NOT NULL"]
        [:username "varchar(32)" "NOT NULL"]
        [:gender "varchar(6)"]
        [:picture "VARCHAR(2083)"]
        [:salt "bytea"]
        [:password "VARCHAR(40)"])))
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
        [:rel_user "integer" "references users(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
        [:rel_role "integer" "references roles(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
        ["PRIMARY KEY (rel_user, rel_role)"])))
  (if-not (table-exist? "tokens" profile)
    (jdbc/db-do-prepared profile
      (ddl/create-table :tokens
        [:rel_user "integer" "UNIQUE" "references users(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
        [:access_token "varchar(36)" "PRIMARY KEY"]
        [:expire "bigint" "NOT NULL"]))))
            
(defn init-db! [profile]
  {:pre [(get allowed-profiles profile)]}
  (->> profile
       (get db-specs)
       (merge {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"})
       pool/make-datasource-spec
       (reset! db)
       create-user-db))
