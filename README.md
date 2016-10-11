# rAPId : build fast and strong APIs on AWS
You can run it with `lein ring server-headless <port>`

## Configuration
Use environment variables :
+ AMZ_ACCESS : your amazon access key
+ AMZ_SECRET : your amazon secret key
+ AMZ_REGION : the region for dynamodb tables (_ex:_ eu-west-1)

## Usage 
See **src/clojure_rest/app.clj** for now.

## Sides effects in AWS
You have to provide a name to your app (_see app-name_).  
The framework will build and/or use the bucket : "rapid-_app-name_", and use tables with _app-name-*_ as prefix.
