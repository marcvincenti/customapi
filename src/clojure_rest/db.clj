(ns clojure-rest.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require ;[clojure.java.jdbc :as jdbc :only [query with-db-transaction]]
            ;[jdbc.pool.c3p0 :as c3p0 :only [make-datasource-spec]]
            ;[clojure-rest.db-utils :refer [create-table]]
            [clojure-rest.db-utilsv2 :refer [create-tables init-objects]]
            [amazonica.aws.s3 :as s3 :only [does-bucket-exist]]))
  
(def bucket "clojure-api-users-2")

(def ^:private objects
  {:users {:keys [
            {:id {:type "Counter" :order-by :creation-date}}
            {:email {:type "String"}}]
           :data [
            {:name {:type "String"}}
            {:password {:type "String"}}
            {:salt {:type "Binary"}}
            {:creation-date {:type "Int"}}
            {:last-connection {:type "Int"}}
            {:picture {:type "String"}}]}})

(def ^:private db-specs
  {:user (or (System/getenv "DATABASE_USER")
              "root")
  :password (or (System/getenv "DATABASE_PASSWORD")
                "toortoor")
  :subname (or (System/getenv "DATABASE_SUBNAME") 
                "//another.ctcyur2o6hny.eu-west-1.rds.amazonaws.com:5432/postgres")
  :classname "org.postgresql.Driver"
  :subprotocol "postgresql"})
  
(defn ^:private aws-setup []
  (do
    ;initializing S3
    (when-not (s3/does-bucket-exist bucket) 
      (s3/create-bucket bucket))
    ;initializing DynamoDB
    (comment
    (create-tables {
        :table-name "users"
        :key-schema
          [{:attribute-name "id" :key-type "HASH"}]
        :attribute-definitions
          [{:attribute-name "id"              :attribute-type "S"}
           {:attribute-name "email"           :attribute-type "S"}]
        :global-secondary-indexes
          [{:index-name "email"
            :key-schema
             [{:attribute-name "email"   :key-type "HASH"}]
            :projection
             {:projection-type "KEYS_ONLY"}
            :provisioned-throughput
             {:read-capacity-units 1 :write-capacity-units 1}
          }]
        :provisioned-throughput
          {:read-capacity-units 1
           :write-capacity-units 1}}))
      (init-objects objects)))

(comment
(defn ^:private create-user-schema [profile]
  (jdbc/with-db-transaction [t-con profile]
    ;create schema 'users'
    (try (jdbc/query t-con "CREATE SCHEMA IF NOT EXISTS users")
      (catch Exception e))
    ;create tables to put in users
    (create-table t-con "users" "users"
      [:id :serial "PRIMARY KEY"]
      [:email "varchar(32)" "UNIQUE" "NOT NULL"]
      [:username "varchar(32)"]
      [:picture "VARCHAR(2083)"]
      [:salt "bytea"]
      [:password "VARCHAR(50)"])
    (create-table t-con "roles" "users"
      [:id "integer" "PRIMARY KEY"]
      [:name "varchar(16)" "NOT NULL" "UNIQUE"])
    (create-table t-con "user_role" "users"
      [:rel_user "integer" "references users.users(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
      [:rel_role "integer" "references users.roles(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
      ["PRIMARY KEY (rel_user, rel_role)"])
    (create-table t-con "tokens" "users"
      [:rel_user "integer" "UNIQUE" "references users.users(id)" "ON DELETE CASCADE" "ON UPDATE CASCADE"]
      [:access_token "varchar(36)" "PRIMARY KEY"]
      [:expire "bigint" "NOT NULL"]))))
            
(defn init! []
  (do
    ;connect and init everything in aws
    (aws-setup)
    ;connect and init database
    (comment
      (-> db-specs
         c3p0/make-datasource-spec
         create-user-schema))))
