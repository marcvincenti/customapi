# Serverless
This project aim to provide a quick and easy way to build services like Firebase, but driven with data (not rules) and serverless (powered by AWS lambda).

You can run it with :
```
lein clean
lein cljsbuild [auto|once] [dev|prod]
lein ring server-headless
```

## What is working now ?
**Nothing** :)  
There is only a back-end and a front-end.   
You can only create/delete projects.
There is also a lot of dead code in the back-end. It's the result of a lot of change in this project which is first a playground project to learn new technos. Some will be probably re-used later, that's why i haven't deleted yet.

## Security
You have to provide your IAM credentials with the following strategy : AmazonAPIGatewayAdministrator.  
They aren't stocked on the web but in your session and in a cookie if you set _remember be_ to true while login.  
This isn't safe at all, because we also provide this credentials each time we have to perform a request to the back-end. But this is for test purposes. This won't be changed until this repo/projec will be publicly accessible.


## TODOs
+ Auth guards
+ Authentication -> IAM ? Cognito ?
+ Database -> DynamoDB
+ Storage -> S3
+ Function -> Lambda

## Requirements

* JDK 1.7+
* Leiningen 2.x

## License

Copyright © 2016 Marc Vincenti

Distributed under the The MIT License (MIT).
