(ns clojure-rest.utils-test
  (:require [clojure.test :refer :all]
            [clojure-rest.utils :refer :all]))

(deftest test-str->int
  (is (thrown? Exception (str->int "hello world")))
  (is (thrown? Exception (str->int "42.1")))
  (is (= 42 (str->int "42"))))

(deftest test-str->float
  (is (thrown? Exception (str->float "hello world")))
  (is (= 42.0 (str->float "42")))
  (is (= 42.0 (str->float "42.0"))))
