(ns aws.dynamoDB-create
  (:require [clojure.test :refer :all]
            [aws.dynamoDB]))
            
(defn create-table 
  [tab]
  (apply (intern 'aws.dynamoDB 'create-table) tab))
            
(deftest test-createTable
  (is (= (create-table {:test {:keys {:id {:type "Index"}}}})
          {:table-name "test"
           :key-schema [{:attribute-name "id" :key-type "HASH"}]
           :attribute-definitions [{:attribute-name "id" :attribute-type "N"}]
           :provisioned-throughput {:read-capacity-units 2
                                    :write-capacity-units 2}}))
  (is (= (create-table
        {:test {:keys {
                  :id {:type "Index" 
                       :order-by :creation-date 
                       :provisioned-throughput {:read-capacity-units 1}}
                  :email {:type "String"
                          :order-by :last-connection }
                  :name  {:type "String"
                          :provisioned-throughput {:write-capacity-units 3}}}
               :data {
                  :password {:type "String"}
                  :salt {:type "Binary"}
                  :creation-date {:type "Integer"}
                  :last-connection {:type "Integer"}
                  :picture {:type "File"}}}})
        {:table-name "test"
         :key-schema [{:attribute-name "id" :key-type "HASH"} 
                      {:attribute-name "creation-date" :key-type "RANGE"}]
         :attribute-definitions [{:attribute-name "id" :attribute-type "N"}
                                 {:attribute-name "email" :attribute-type "S"} 
                                 {:attribute-name "name" :attribute-type "S"} 
                                 {:attribute-name "creation-date" :attribute-type "N"} 
                                 {:attribute-name "last-connection" :attribute-type "N"}]
         :provisioned-throughput {:read-capacity-units 1 :write-capacity-units 2}
         :global-secondary-indexes [{:index-name "index_email" 
                                     :key-schema [{:attribute-name "email" :key-type "HASH"} 
                                                  {:attribute-name "last-connection" :key-type "RANGE"}]
                                     :projection {:projection-type "ALL"}
                                     :provisioned-throughput {:read-capacity-units 2 :write-capacity-units 2}} 
                                    {:index-name "index_name" 
                                     :key-schema [{:attribute-name "name" :key-type "HASH"}]
                                     :projection {:projection-type "ALL"}
                                     :provisioned-throughput {:read-capacity-units 2 :write-capacity-units 3}}
                                    ]})))
