(ns clojure-rest.data-verification-test
  (:require [clojure.test :refer :all]
            [clojure-rest.data-verification :refer :all]
            [clojure-rest.data-utils :refer :all]))

(deftest test-isString?
  (is (nil? (isString? "test")))
  (is (= "Not a valid string." 
        (isString? 1)))
  (is (= "test" 
        (isString? 1 :errmsg "test")))
  (is (= "I prefer letters." 
        (isString? "1234" :regex "[a-zA-Z]*" :errmsg "I prefer letters."))))

(deftest test-check-object-normal
  (is (nil? (check {:data "testing@test.test" :function email-address? :dataname "email" :required true})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function email-address? :dataname "email" :required true})))
  (is (= "Missing required value : \"email\"." 
    (check {:data nil :function email-address? :dataname "email" :required true}))))
    
(deftest test-check-object-nil
  (is (nil? (check {:data "testing@test.test" :function email-address?})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function email-address?})))
  (is (nil? (check {:data nil :function email-address?}))))

(deftest test-check-object-multifunc
  ;and
  (is (nil? (check {:data "testing@test.test" :function [:and isString? email-address?]})))
  (is (= "The \"username\" can't contain special characters and has to be between 4 and 32 characters." 
    (check {:data "testing@test.test" :function [:and username? email-address?]})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function [:and username? email-address?]})))
  (is (= "The \"username\" can't contain special characters and has to be between 4 and 32 characters.\n\"email\" is not RFC 2822 compliant." 
    (check {:data 1 :function [:and username? email-address?]})))
  ;or
  (is (nil?
    (check {:data "testing@test.test" :function [:or username? email-address?]})))
  (is (nil? 
    (check {:data "testing" :function [:or username? email-address?]})))
  (is (= "The \"username\" can't contain special characters and has to be between 4 and 32 characters."
    (check {:data 1 :function [:or username? email-address?]})))
  ;nested
  (is (nil? (check {:data "testing@test.test" :function [:and [:or isString? username?] email-address?]})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function [:and [:or isString? username?] email-address?]}))))

(deftest test-check-object-multidata
  (is (nil? (check {:data "testing@test.test" :function email-address?}
                   {:data nil :function isString?}
                   {:data "testing" :function username?})))
  (is (= "\"email\" is not RFC 2822 compliant.\nThe \"username\" can't contain special characters and has to be between 4 and 32 characters." 
        (check {:data "testing" :function email-address?}
               {:data nil :function isString?}
               {:data "testing@test.test" :function username?}))))
