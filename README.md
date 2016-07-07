# Users clojure REST Api
You can run it :
 + locally with `lein ring server-headless <port>`
 + on [Heroku](https://www.heroku.com/) with the following Procfile : 
```
web: lein ring server-headless
```
## Configuration
This section will be completed soon
## Usage
#### Connection
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
 - [facebook](https://www.facebook.com/)
 - [google](https://www.google.com/)
