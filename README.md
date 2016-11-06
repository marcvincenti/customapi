# Whatever name it has
You can run it with :
```
lein clean
lein cljsbuild [auto|once] [dev|prod]
lein ring server-headless
```

## TODOs
+ Add a menu
+ Connection (sort of?) -> including looking for project
+ Objects page (to update dynamoDB tables)
+ Update dynamoDB tables
+ Include Cognito
+ REST-handler - With no links (verification+dynamodb; s3; cognito)
+ Use verifications (i.e. finish collate everything)
+ Let user add custom codes (wrappers, routines & routes)

## Configuration
Use environment variables :
+ AWS_ACCESS_KEY : your amazon access key
+ AWS_SECRET_ACCESS_KEY : your amazon secret key
+ AWS_DEFAULT_REGION : the region for dynamodb tables (_ex:_ eu-west-1)

## Requirements

* JDK 1.7+
* Leiningen 2.x

## License

Copyright © 2016 Marc Vincenti

Distributed under the The MIT License (MIT).
