# My clojure REST Api
You can run it :
 + locally with `lein ring server-headless <port>`
 + on [Heroku](https://www.heroku.com/) with the following Procfile : 
```
web: lein ring server-headless
```

### Configuration
This is using a PostgreSQL database and an [Amazon AWS](https://aws.amazon.com/fr/console/) account.  
 + Use environment variables with the following names : DATABASE_USER, DATABASE_PASSWORD and DATABASE_SUBNAME.  
 
Because of amazon bucket policy, you have to set your buckets names
(you can just add your corporation name before each bucket name for example) 
_"src/clojure_rest/db.clj"_ at the **buckets** definition and 
you have to configure an amazon connection with [IAM](https://console.aws.amazon.com/iam/home).  
 + Use environment variables with the following names : AMZ_ACCESS, AMZ_SECRET and AMZ_ENDPOINT. 


# Usage 

### Connection
Return a private *user* object following this model :
```
{
	name : "John Doe"
	picture : "path_to_your_profile_pic"
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
 + password (string)
 + username (string) - optionnal
 + picture (url#string or a file) - optionnal
 
#### Deleting user
You can delete a user profile with **[DELETE] -> /me/**   
But you have to provide an *access_token* in the url **AND** your password (string) to successfully remove the user account.

#### Updating user
You can update a user profile with **[PUT] -> /me/** while providing an *access_token*.  
However, to change the *password* field, you have to provide an *oldpassword* (string) **and** a *newpassword* (string).
This will return a field named *passwordUpdate* (boolean).
 
## Usefull stuff
You can request api for specific things like :
 + is email already taken ? **[GET] -> /test/email/&lt;string>** (case insensitive)
