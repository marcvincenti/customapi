# Serverless & microservices
This project aim to provide a quick and easy way to build services like Firebase, but value oriented (inspired by [this talk](https://www.youtube.com/watch?v=-6BsiVyC1kM)) and serverless (powered by AWS). The next step will be sharing public data between different platforms.

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

## Review
My biggest problem is the front-end. I don't nderstand everything, i've just moved to react and i don't know how to use it properly. The code (front) is organized like in angular2 (that's what i know) but it doen't fit well for react.  
There is 4 parts/folders :

 - **app/**
	 - *core.cljs* : initialization of the app and routing
	 - *state.cljs* : a big ratom to stock values shared all over the app
 - **components/**
   - *alert/alert.cljs* : basic code to use bootstrap alert components without rewriting them everywhere
   - *menu_bar/menu_bar.cljs* : The navbar on top of every pages in the app
 - **pages/**
   - *about/about.cljs* : This page only show the big ratom from state.cljs (actually used for debug)
   - *home/home.cljs* : Do I really have to explain this ?
   - *login/login.cljs* : ...
   - *project_details/project_details* : This will be the page where you can control a rest api (actuall show the rest api name)
   - *projects/projects.cljs* : the page where you can create, list and delete your apis.
 - **providers/**
   - *auth/auth.cljs* : The controller for user authentication in app
   - *cookies/cookies.cljs* : The controller to save and load data from cookies.
   - *projects/projects.cljs* : The controller to create/delete/get Rest apis.

So here are my questions :

  - Is it a good organization ?
  - How to pass properly params in navigation ?
  - Is there a way to build auth guards in routing ? (like if i'm authenticated within the app, when i request the login page, i am automatically redirected to projects page or if i'm not connected and i want to see my projects, i'm redirected to the login page)
  - Is there something you had done another way if you were doing these ?
  - Any good advice ?

Thank you ;)

## Security (front-end)
You have to provide some IAM credentials with the following strategy : AmazonAPIGatewayAdministrator.  
They aren't stocked on the web but in your session and in a cookie if you set _remember be_ to true while login.  
This isn't safe at all, because we also provide this credentials each time we have to perform a request to the back-end. But this is for test purposes. This won't be changed until this repo/project will be publicly accessible or in an open beta. Feel free to propose any better solution.

## Basic functions
The first goal is to let user user build serverless api like this :
![Serverless schema on AWS] (serverless-app.png)

## TODO(s)
+ Auth guards
+ Authentication -> IAM ? Cognito ?
+ Database -> DynamoDB
+ Storage -> S3
+ Function -> Lambda
+ Security rules

## Requirements

* JDK 1.7+
* Leiningen 2.x

## License

Copyright © 2016 Marc Vincenti

Distributed under the The MIT License (MIT).
