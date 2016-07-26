# Users clojure REST Api
You can run it :
 + locally with `lein ring server-headless <port>`
 + on [Heroku](https://www.heroku.com/) with the following Procfile : 
```
web: lein ring server-headless
```

### Configuration
This is using a PostgreSQL database and an [Amazon AWS](https://aws.amazon.com/fr/console/) account.  
Because of amazon bucket policy, you have to set your buckets names (you can just add your corporation name before each bucket name for example) _"src/clojure_rest/db.clj"_ at the **buckets** definition

#### Tests
 + You have to create a Postgres database and put the credentials in _"src/clojure_rest/db.clj"_ in the **db-specs** definition.
 + There is also an amazon connection, you have to configure an amazon connection with [IAM](https://console.aws.amazon.com/iam/home) 
    + and then put credentials in _"src/clojure_rest/db.clj"_ in the **conn-specs** definition. 
    
#### Production
This part will come later. 

# Usage 

### Connection
Return a private *user* object following this model :
```
{
	name : "John Doe"
	picture : "path_to_your_profile_pic"
	gender : "male"
	email : "address@whatever.com"
	access_token : "..."
}
```
You can connect with **[GET] -> /oauth/&lt;platform>/&lt;access_token>/**   
The following platforms are supported :
 + [facebook](https://www.facebook.com/)
 + [google](https://www.google.com/)
 
Or using **[POST] -> /login/**   
with this values :
 + email (string), also accept a username in this field
 + password (string)
 
### Disconnection
You can disconnect yourself using **[POST] -> /logout/** while providing an *access_token*.
 
## Object "me"
You can get your profile informations with **[GET] -> /me/**  
But you have to provide an *access_token* in the url.

#### Subscribtion
Return a private *user* object following the precedent model.  
You can subscribe an user with **[POST] -> /me/**
using the following values :
 + email (string)
 + username (string)
 + password (string)
 + gender ("male" or "female" or _NULL_) - optionnal
 + picture (url#string or a file) - optionnal
 
## Usefull stuff
You can request api for specific things like :
 + is username already taken ? **[GET] -> /test/username/&lt;string>** (case insensitive)
 + is email already taken ? **[GET] -> /test/email/&lt;string>** (case insensitive)
