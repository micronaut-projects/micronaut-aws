# Micronaut Star Wars Quiz

This is a Micronaut Alexa demo app written in Groovy that asks the user 5 questions randomly drawn from a DynamoDB table. 
It doesn't tell you the answers but will tell you at the end how many you got correct. If you have a display device it should show a cool star background.

The skill also gathers usage statistics and logs them into a DynamoDB table so you can see how the skill is being used.


The application can be build with either Gradle (which builds the JAR to the `build/libs` directory):


This app requires database tables set up in DynamoDB with the quiz questions. 


```bash
./gradlew shadowJar
./gradlew deploy
```

Do steps 1 and 2 at the same time in 2 browser tabs:


1) go to developer.amazon.com -> Alexa -> Skills -> Create New Skills
create app name, invocation name (try 'micronaut star wars quiz'). Copy down Skill ID for step below.


2) go to The AWS Console and find the Lambda you just deployed. Copy down the ARN for it. Add a trigger for Alexa,
and put in the skill ID from the above step and save. Take the lambda ARN, and add that to the default endpoints for step 1 above.


3) go back to the custom skills console on developer.amazon.com. On the left side, under Interaction Model click on 
JSON Editor.
 
paste in contents of interactionModel.json from /src/main/resources
set account linking = no
put defaults in under privacy
save

4)
go back to AWS Console - go to DynamoDB
we will need 3 tables:
StarWarsQuiz
StarWarzQuizMetrics
StarWarsQuizUserMetrics

Set up databases, we suggest using import for 'StarWarsQuiz'. You'll have to copy the import data from /src/main/resources/dynamoDB to an S3 bucket or import via AWS CLI and import from there
The files are broken up into 3 files (batch write item has a small limit per call)
aws dynamodb batch-write-item --request-items file://StarWars1.json
aws dynamodb batch-write-item --request-items file://StarWars2.json
aws dynamodb batch-write-item --request-items file://StarWars3.json


The other 2 tables you can manually create them in AWS DynamoDB Console:
StarWarzQuizMetrics - select create table -> 'id' type number, use default settings checked
StarWarzQuizUserMetrics - select create table -> 'id' type number, use default settings checked

5) on developer console, go to build model, then go to test tab and type 'open micronaut star wars quiz' (or whatever invocation name you used)


Or Maven which builds the JAR to the `target` directory:

```bash
./mvnw package
```
From there to deploy with maven follow this guide to install the archtype: https://aws.amazon.com/blogs/compute/from-framework-to-function-deploying-aws-lambda-functions-for-java-8-using-apache-maven-archetype/


To set up the quiz you still need to grant DynamoDB read/write access to the IAM Role your lambda is running as.

