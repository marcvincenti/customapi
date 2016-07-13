(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [clojure.java.jdbc :as jdbc]
            [clojure-rest.db-utils :refer [table-exist?]]
            [jdbc.pool.c3p0 :as pool]
            [java-jdbc.ddl :as ddl]
            [amazonica.aws.s3 :as s3]))

(def db (atom nil))
(def conn (atom nil))
(def current-profile (atom nil))

(def ^:private allowed-profiles #{:prod :test})

(defn set-profile! [profile]
  {:pre [(get allowed-profiles profile)]}
  (reset! current-profile profile))
  
(def buckets 
  {:users-profiles "clojure-api-users-profiles"})

(def ^:private db-specs
  {:prod {:user (System/getenv "DATABASE_USER")
          :password (System/getenv "DATABASE_PASSWORD")
          :subname (System/getenv "DATABASE_SUBNAME")
          }
   :test {:user "root"
          :password "toortoor"
          :subname "//another.ctcyur2o6hny.eu-west-1.rds.amazonaws.com:5432/postgres"
          }})
          
(def ^:private conn-specs
  {:prod {:access-key (System/getenv "AMAZON_ACCESS")
          :secret-key (System/getenv "AMAZON_KEY")
          :endpoint (System/getenv "AMAZON_ENDPOINT")
          }
   :test {:access-key "AKIAIKZWOA4I5Y43GDOA"
          :secret-key "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"
          :endpoint   "eu-west-1"
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
 
(defn ^:private amazon-setup [profile]
  (doseq [[k v] (map identity buckets)] 
    (when-not (s3/does-bucket-exist profile v) 
      (s3/create-bucket profile v))))
            
(defn init-db! [profile]
  {:pre [(get allowed-profiles profile)]}
  (do 
    ;connect and build Postgres users database
    (->> profile
       (get db-specs)
       (merge {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"})
       pool/make-datasource-spec
       (reset! db)
       create-user-db)
    ;connect to amazon
    (->> profile
       (get conn-specs)
       (reset! conn)
       amazon-setup)))
