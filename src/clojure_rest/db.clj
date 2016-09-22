(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.db-utils :refer [table-exist?]]
            [jdbc.pool.c3p0 :as pool]
            [java-jdbc.ddl :as ddl]))
  
(def bucket "clojure-api-users")

(def ^:private db-specs
  {:user (or (System/getenv "DATABASE_USER")
              "root")
  :password (or (System/getenv "DATABASE_PASSWORD")
                "toortoor")
  :subname (or (System/getenv "DATABASE_SUBNAME") 
                "//another.ctcyur2o6hny.eu-west-1.rds.amazonaws.com:5432/postgres")
  :classname "org.postgresql.Driver"
  :subprotocol "postgresql"})

(defn ^:private create-user-db [profile]
  (if-not (table-exist? "users" profile)
    (jdbc/db-do-prepared profile
      (ddl/create-table :users
        [:id :serial "PRIMARY KEY"]
        [:email "varchar(32)" "UNIQUE" "NOT NULL"]
        [:username "varchar(32)"]
        [:picture "VARCHAR(2083)"]
        [:salt "bytea"]
        [:password "VARCHAR(50)"])))
  (if-not (table-exist? "roles" profile)
    (do
      (jdbc/db-do-prepared profile
        (ddl/create-table :roles
          [:id "integer" "PRIMARY KEY"]
          [:name "varchar(16)" "NOT NULL" "UNIQUE"]))
      (jdbc/insert! profile :roles {:id 100 :name "verified"})
      (jdbc/insert! profile :roles {:id 200 :name "admin"})))
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
            
(defn init! []
  ;connect and build Postgres users database
  (-> db-specs
     pool/make-datasource-spec
     create-user-db))
