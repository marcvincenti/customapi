(ns clojure-rest.valid)

(defn check
  "Check if a value is correct or nil if required is
   false and return an error string message"
  [{:keys [data function dataname required] :or {dataname "UNKNOW" required false}}]
  (if data
    (function data)
    (if required
      (str "Missing required value : \"" dataname "\".")
      nil)))

(defn check-all
  "check all the values passed in parameter and return an error string"
  [& args]
    (let [wrong-entries (reduce #(let [ret (check %2)] (if ret (conj %1 ret))) [] args)]
      (if (empty? wrong-entries)
        nil
        (clojure.string/join "\n" wrong-entries))))
      
(defn xemail-address?
  "Return string error if the email address is not valid, based on RFC 2822."
  [email]
  (if (string? email)
	  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
					"(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
					"@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
					"[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
      (if (boolean (re-matches (re-pattern re) email))
        nil
        "\"email\" is not RFC 2822 compliant."))
    "\"email\" have to be a string."))
    
(defn xusername?
  "Return string error if the username is not valid"
  [uname]
  (if (string? uname)
	  (let [re (str "[a-zA-Z-_ ’'‘ÆÐƎƏƐƔĲŊŒẞÞǷȜæðǝəɛɣĳŋœĸſßþƿȝ"
                  "ĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţț"
                  "ŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔ"
                  "ĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗð"
                  "éèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶ"
                  "ƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîï"
                  "ǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœ"
                  "ŔŘŖŚŜŠŞȘṢẞŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸ"
                  "ȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃ"
                  "ẁŵẅƿýỳŷÿȳỹƴźżžẓ0-9]{4,32}")]
      (if (boolean (re-matches (re-pattern re) uname))
        nil
        "The \"username\" can't contain special characters and has to be between 4 and 32 characters."))
    "\"username\" have to be a string."))
    
(defn xpic-uri?
  "Return a string error if picture uri isn't RFC 2396 compliant."
  [uri]
  (let [re (str "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|jpeg|png))(?:\\?([^#]*))?(?:#(.*))?")]
    (if (boolean (re-matches (re-pattern re) uri))
      nil
      "\"picture\"'s URI is not RFC 2396 compliant.")))

(defn xpic-file? 
  [{:keys [content-type]}]
  (if (or (= content-type "image/jpeg") (= content-type "image/png"))
    nil
    "The \"picture\" doesn't seem to be a jpeg or a png or an uri."))
    
(defn xpic?
  "return a string error if picture isn't a valid uri or valid file"
  [x]
  (if (string? x)
    (xpic-uri? x)
    (xpic-file? x)))
    
(defn xgender?
  "Return string error if the gender isn't equal to 'male' or 'female'"
  [gender]
  (if (string? gender)
    (let [re (str "male|female")]
      (if (boolean (re-matches (re-pattern re) gender))
        nil
        "\"gender\" have to be equal to 'male' or 'female'."))
    "\"gender\" have to be a string."))

(defn pic-file? 
  [x]
  (if (nil? x)
    false
    (or 
      (= (:content-type x) "image/jpeg")
      (= (:content-type x) "image/png"))))

(defn email-address?
  "Returns true if the email address is valid, based on RFC 2822."
  [email]
  (if (string? email)
	  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
					"(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
					"@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
					"[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
      (boolean (re-matches (re-pattern re) email)))
    false))
    
(defn username?
  "Returns true if the username is correct"
  [uname]
  (if (string? uname)
	  (let [re (str "[a-zA-Z-_ ’'‘ÆÐƎƏƐƔĲŊŒẞÞǷȜæðǝəɛɣĳŋœĸſßþƿȝ"
                  "ĄƁÇĐƊĘĦĮƘŁØƠŞȘŢȚŦŲƯY̨Ƴąɓçđɗęħįƙłøơşșţț"
                  "ŧųưy̨ƴÁÀÂÄǍĂĀÃÅǺĄÆǼǢƁĆĊĈČÇĎḌĐƊÐÉÈĖÊËĚĔ"
                  "ĒĘẸƎƏƐĠĜǦĞĢƔáàâäǎăāãåǻąæǽǣɓćċĉčçďḍđɗð"
                  "éèėêëěĕēęẹǝəɛġĝǧğģɣĤḤĦIÍÌİÎÏǏĬĪĨĮỊĲĴĶ"
                  "ƘĹĻŁĽĿʼNŃN̈ŇÑŅŊÓÒÔÖǑŎŌÕŐỌØǾƠŒĥḥħıíìiîï"
                  "ǐĭīĩįịĳĵķƙĸĺļłľŀŉńn̈ňñņŋóòôöǒŏōõőọøǿơœ"
                  "ŔŘŖŚŜŠŞȘṢẞŤŢṬŦÞÚÙÛÜǓŬŪŨŰŮŲỤƯẂẀŴẄǷÝỲŶŸ"
                  "ȲỸƳŹŻŽẒŕřŗſśŝšşșṣßťţṭŧþúùûüǔŭūũűůųụưẃ"
                  "ẁŵẅƿýỳŷÿȳỹƴźżžẓ0-9]{4,32}")]
      (boolean (re-matches (re-pattern re) uname)))
    false))
    
(defn image-uri?
  "Returns true if picture uri is valid, based on RFC 2396."
  [uri]
  (if (string? uri)
	  (let [re (str "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|jpeg|png))(?:\\?([^#]*))?(?:#(.*))?")]
      (boolean (re-matches (re-pattern re) uri)))
    false))

(defn gender?
  "Returns true if the gender is equal to 'male' or 'female'"
  [gender]
  (let [re (str "male|female")]
    (boolean (re-matches (re-pattern re) gender))))



