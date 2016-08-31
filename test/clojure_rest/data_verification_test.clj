(ns clojure-rest.data-verification-test
  (:require [clojure.test :refer :all]
            [clojure-rest.data-verification :refer :all]))

(deftest test-xstring?
  (is (nil? (xstring? "test")))
  (is (= "Not a string." 
        (xstring? 1)))
  (is (= "test" 
        (xstring? 1 :errmsg "test")))
  (is (= "I prefer letters." 
        (xstring? "1234" :errmsg "I prefer letters." :regex "[a-zA-Z]*"))))

(deftest test-check-object-normal
  (is (nil? (check {:data "testing@test.test" :function xemail-address? :dataname "email" :required true})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function xemail-address? :dataname "email" :required true})))
  (is (= "Missing required value : \"email\"." 
    (check {:data nil :function xemail-address? :dataname "email" :required true}))))
    
(deftest test-check-object-nil
  (is (nil? (check {:data "testing@test.test" :function xemail-address?})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function xemail-address?})))
  (is (nil? (check {:data nil :function xemail-address?}))))

(deftest test-check-object-multifunc
  ;and
  (is (nil? (check {:data "testing@test.test" :function [:and xstring? xemail-address?]})))
  (is (= "The \"username\" can't contain special characters and has to be between 4 and 32 characters." 
    (check {:data "testing@test.test" :function [:and xusername? xemail-address?]})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function [:and xusername? xemail-address?]})))
  (is (= "\"username\" have to be a string.\n\"email\" have to be a string." 
    (check {:data 1 :function [:and xusername? xemail-address?]})))
  ;or
  (is (nil?
    (check {:data "testing@test.test" :function [:or xusername? xemail-address?]})))
  (is (nil? 
    (check {:data "testing" :function [:or xusername? xemail-address?]})))
  (is (= "\"username\" have to be a string." 
    (check {:data 1 :function [:or xusername? xemail-address?]})))
  ;nested
  (is (nil? (check {:data "testing@test.test" :function [:and [:or xstring? xusername?] xemail-address?]})))
  (is (= "\"email\" is not RFC 2822 compliant." 
    (check {:data "testing" :function [:and [:or xstring? xusername?] xemail-address?]}))))

(deftest test-check-object-multidata
  (is (nil? (check {:data "testing@test.test" :function xemail-address?}
                   {:data nil :function xstring?}
                   {:data "testing" :function xusername?})))
  (is (= "\"email\" is not RFC 2822 compliant.\nThe \"username\" can't contain special characters and has to be between 4 and 32 characters." 
        (check {:data "testing" :function xemail-address?}
               {:data nil :function xstring?}
               {:data "testing@test.test" :function xusername?}))))
