(ns clojure-rest.data-verification)

(defn ^:private run-checks
  "run the multiples checks to do, data is not nil and func is a function or a list of functions"
  [func data]
    (if (vector? func)
      (let [seq-errors (reduce #(conj %1 (run-checks %2 data)) [] (rest func))]
        (case (first func)
          :or (when (not-any? nil? seq-errors) (first seq-errors))
          :and (let [seq-errors-only (remove nil? seq-errors)]
                  (when-not (empty? seq-errors-only) (clojure.string/join "\n" seq-errors-only)))))
      (-> data func)))

(defn check
  "check all the values passed in parameter with run-checks and return a concatened error string"
  [& args]
    (let [wrong-entries 
      (remove nil? 
        (reduce #(let [{:keys [data function dataname required] 
                        :or {dataname "UNKNOW" required false}} %2
                      ret (if data
                            (run-checks function data)
                            (when required
                              (str "Missing required value : \"" dataname "\".")))]
                          (conj %1 ret)) [] args))]
      (when-not (empty? wrong-entries)
        (clojure.string/join "\n" wrong-entries))))
      
(defn isString?
  [x & {:keys [errmsg regex] :or {errmsg "Not a valid string."}}]
  (when (or (not (string? x))
          (and regex
               (not (boolean (re-matches (re-pattern regex) x))))) 
    errmsg))

(defn xpic-file? 
  [{:keys [content-type]}]
  (when-not (or (= content-type "image/jpeg") (= content-type "image/png"))
    "The \"picture\" doesn't seem to be a jpeg or a png or an uri."))
