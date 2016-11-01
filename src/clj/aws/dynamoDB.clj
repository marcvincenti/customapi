(ns aws.dynamoDB
	(:require [clojure.set :refer [intersection difference]]
            [amazonica.aws.dynamodbv2 :as ddb
              :only [create-table list-tables delete-table describe-table]]))

(defn ^:private dynamoDB-types
  "Translate a custom type (string) to the corresponding dynamodb type"
  [customType]
  (case customType
      ("Binary") "B"
      ("Index" "Date" "Integer" "Float" "Double") "N"
      ("File" "String" "Char") "S"))

(defn ^:private dynamoDB-provisioned-throughput
  "Return a dynamoDB provisioned-throughput map
   correspondind to the keymap parameter
   default value are 2 writers & 2 readers"
  [keymap]
  (let [default-read-capacity 2
        default-write-capacity 2
        {:keys [read-capacity-units write-capacity-units]
         :or {read-capacity-units default-read-capacity
              write-capacity-units default-write-capacity}}
          (:provisioned-throughput keymap)]
    {:read-capacity-units read-capacity-units
    :write-capacity-units write-capacity-units}))

(defn ^:private dynamoDB-key-schema
  "Return a vector containing the key schema for dynamoDB"
  [obj]
  (let [obj-keys (:keys (second obj))
        hash-var [{:attribute-name (-> obj-keys first first name)
                   :key-type "HASH"}]
        range-var (:order-by (-> obj-keys first second))]
    (if range-var
      (conj hash-var {:attribute-name (name range-var)
                      :key-type "RANGE"})
                hash-var)))

(defn ^:private dynamoDB-attribute-definitions
  "Return set of attribute definitions for dynamoDB"
  [obj]
  (let [obj-keys (:keys (second obj))
        infos (conj obj-keys (:data (second obj)))]
            (into [] (for [k (remove nil? (distinct (concat
                      (keys obj-keys)
                      (map :order-by (vals obj-keys)))))]
              {:attribute-name (name k)
               :attribute-type (dynamoDB-types (:type (get infos k)))}))))

(defn ^:private dynamoDB-global-secondary-indexes
  "Return set of global secondary indexes for dynamoDB"
  [obj]
  (reduce (fn [lst index]
    (let [range-var (:order-by (second index))
          hash-var [{:attribute-name (-> index first name) :key-type "HASH"}]
          key-schema (if-not range-var hash-var
                          (conj hash-var {:attribute-name (name range-var)
                                          :key-type "RANGE"}))]
      (conj lst {:index-name (str "index_" (-> index first name) (if range-var (str "_" (name range-var))))
                 :key-schema key-schema
                 :projection {:projection-type "ALL"}
                 :provisioned-throughput (dynamoDB-provisioned-throughput (second index))})))
        [] (rest (:keys (second obj)))))

(defn ^:private create-table
  "Create tables to store an object described in obj in a new dynamodb table"
  [obj]
  (let [obj-keys (:keys (second obj))
        tablemap {:table-name (name (first obj))
                  :key-schema (dynamoDB-key-schema obj)
                  :attribute-definitions (dynamoDB-attribute-definitions obj)
                  :provisioned-throughput (dynamoDB-provisioned-throughput (-> obj-keys first second))}]
  (if (empty? (rest obj-keys)) tablemap
    (assoc tablemap :global-secondary-indexes (dynamoDB-global-secondary-indexes obj)))))

(defn ^:private update-table
  "Update table to store the parameter object
   described in obj in the corresponding table"
  [obj aws-table]
  (let [table-name (name (first obj))
        in-aws-table (get aws-table :table)
        update-fn (fn [originals news del-fn add-fn]
                    (let [original-set (set originals)
                          new-set (set news)
                          to-keep (intersection original-set new-set)
                          to-delete (difference original-set to-keep)
                          to-add (difference new-set to-keep)]
                      (do
                        (if-not (empty? to-delete) (del-fn to-delete))
                        (if-not (empty? to-add) (add-fn to-add)))))]
    (update-fn (:attribute-definitions in-aws-table)
               (dynamoDB-attribute-definitions obj)
               (fn [s] (println (str "\tTo Delete  -> " s)))
               (fn [s] (println (str "\tTo Add     -> " s))))))

(defn set-db
  "Take a map of objects in parameters
   and update database to support this objects"
  [app-name tables-map]
  (let [ddb-tables (filter #(re-matches (re-pattern (str "^" app-name "-(.*)$")) %)
                      (:table-names (ddb/list-tables)))
        objects-map (into {} (for [[k v] tables-map]
                              [(keyword (str app-name "-" (name k))) v]))]
    (doseq [tab (reduce-kv (fn [ddb-tables k v]
                  (let [tab-name (-> k name)]
                    (if-not (some #{tab-name} ddb-tables)
                      (do
                        (println (str "Create table \"" tab-name "\"."))
                        (-> {k v} first create-table ddb/create-table future)
                        ddb-tables)
                      (do (println (str "Update table \"" tab-name "\"."))
                        (-> {k v} first (update-table (ddb/describe-table :table-name tab-name)))
                        (filter #(not= % tab-name) ddb-tables)))))
                  ddb-tables objects-map)]
      (do (println (str "Delete table \"" tab "\"."))
        (->> tab (ddb/delete-table :table-name) future)))))
