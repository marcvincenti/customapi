(ns clojure-rest.verification.data-types
  (:require [clojure.test :refer :all]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.util.io :refer [string-input-stream]]
            [clojure-rest.data-verification :refer :all]))

(deftest test-isString?
  (is (nil? (isString? "test")))
  (is (= "Not a valid string." 
        (isString? 1)))
  (is (= "test" 
        (isString? 1 :errmsg "test")))
  (is (= "I prefer letters." 
        (isString? "1234" :regex "[a-zA-Z]*" :errmsg "I prefer letters."))))
        
(deftest test-isNumber?
  (is (nil? (isNumber? 1)))
  (is (= "Not a number." 
        (isNumber? "foo")))
  (is (= "test" 
      (isNumber? "bar" :errmsg "test")))
  (is (= "Too high." 
        (isNumber? 1337 :max 42 :errmsg "Too high."))))
        
(deftest test-isFile?
  (let [form-body (str "--XXXX\r\n"
                       "Content-Disposition: form-data;"
                       "name=\"upload\"; filename=\"test.txt\"\r\n"
                       "Content-Type: text/plain\r\n\r\n"
                       "foo\r\n"
                       "--XXXX\r\n"
                       "Content-Disposition: form-data;"
                       "name=\"baz\"\r\n\r\n"
                       "qux\r\n"
                       "--XXXX--")
        handler (wrap-multipart-params identity)
        request {:headers {"content-type" "multipart/form-data; boundary=XXXX"
                           "content-length" (str (count form-body))}
                 :params {"foo" "bar"}
                 :body (string-input-stream form-body)}
        response (handler request)
        upload (get-in response [:params "upload"])]
    (is (nil? (isFile? upload)))
    (is (= "Not a file." 
      (isFile? 1)))
    (is (= "test" 
        (isFile? 1 :errmsg "test")))
    (is (nil? (isFile? upload :types ["text/plain"])))
    (is (= "I prefer pictures." 
        (isFile? upload :types ["image/png" "image/jpeg"] :errmsg "I prefer pictures.")))))
