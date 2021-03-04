## Micronaut AWS Parameter Store as configuration source

Application uses AWS Parameter for Configuration Discovery.

## How to run
1. Configure AWS CLI
2. Create AWS Parameters :
```bash
   aws ssm put-parameter --name /config/aws-parameter-store-example/owners/barney/name --value=Barney --type String
   aws ssm put-parameter --name /config/aws-parameter-store-example/owners/barney/age --value=33 --type String
   aws ssm put-parameter --name /config/aws-parameter-store-example/owners/barney/pets --value=Marty,Dino --type StringList
```
3. Run application from root project `MICRONAUT_ENVIRONMENTS=ec2 ./gradlew aws-parameter-store-example:clean aws-parameter-store-example:run`

