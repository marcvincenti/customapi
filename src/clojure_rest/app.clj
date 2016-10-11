(ns clojure-rest.app)
  
(def app-name 
  "app-test")

(def objects
  "Our objects definitions"
  {:users {:keys {
              :id {:type "Index" 
                   :order-by :creation-date 
                   :provisioned-throughput {:read-capacity-units 1}}
              :email {:type "String"}}
           :data {
              :name {:type "String"}
              :password {:type "String"}
              :salt {:type "Binary"}
              :creation-date {:type "Integer"}
              :last-connection {:type "Integer"}
              :picture {:type "String"}}}
  :qsdds {:keys {
              :id {:type "Index"}}
           :data {
              :name {:type "String"}
              :password {:type "String"}
              :salt {:type "Binary"}
              :creation-date {:type "Integer"}
              :last-connection {:type "Integer"}
              :picture {:type "String"}}}})
