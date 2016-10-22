# rAPId : build fast and strong APIs on AWS
You can run it with `lein ring server-headless <port>`

## TODOs
+ Check if the bucket you want to use is not owned by someone else
+ Update dynamoDB tables
+ Include Cognito + IAM + Lambda
+ Clean up the mess
+ REST-handler
+ Use verifications (i.e. finish collate everything)
+ Let user add custom codes (wrappers, routines & routes)

## Configuration
Use environment variables :
+ AWS_ACCESS_KEY : your amazon access key
+ AWS_SECRET_ACCESS_KEY : your amazon secret key
+ AWS_DEFAULT_REGION : the region for dynamodb tables (_ex:_ eu-west-1)

## Usage 
See **clojure_rest/init** for now.

## Sides effects in AWS
You have to provide a name to your app (_see app-name_).  
The framework will build and/or use the bucket : "rapid-_app-name_", and use tables with _app-name-*_ as prefix.
