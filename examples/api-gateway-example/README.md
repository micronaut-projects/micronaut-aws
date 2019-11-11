# my-service serverless API

The starter project defines a simple `/ping` resource that can accept `GET` requests with its tests.

The project folder also includes a `sam.yaml` file.
You can use this [SAM](https://github.com/awslabs/serverless-application-model) file to deploy the project to AWS Lambda and Amazon API Gateway.

A special version of the [SAM](https://github.com/awslabs/serverless-application-model) file (`sam-local.yml`) is provided to run with [SAM Local](https://github.com/awslabs/aws-sam-local).
The reason for the special version is convenience for local testing - the only difference between `sam.yml` and `sam-local.yml` is the value for `CodeUri`;
In `sam.yml` it is the fat jar file that is created with the gradle task `shadowJar`, in `sam-local.yml` it is an exploded directory with the application class files and a lib directory containing all the runtime dependencies.
When running SAM Local, if you used the fat jar, then every invocation would require the fat jar to be expanded, which takes a long time because every dependency jar is expanded when you do this.
By having the build create the `exploded` directory, that expansion is avoided and the local invocation is much faster.
Not only is the invocation faster, but it is also sensitive to rebuilds of the project -- each time you `./gradlew shadowJar`, the `exploded` directory is updated and any subsequent invocations of an endpoint in the api reflect the new code without having to re-invoke SAM Local.

You can use [AWS SAM Local](https://github.com/awslabs/aws-sam-local) to start your project.

First, install SAM local:

```bash
$ npm install -g aws-sam-local
```

or on a *nix machine with Home Brew

```bash
brew install aws/tap/aws-sam-cli
```


Next, from the project root folder - where the `sam-local.yaml` file is located - start the API with the SAM Local CLI.

```bash
$ sam local start-api --template sam-local.yaml

...
Mounting com.amazonaws.serverless.archetypes.StreamLambdaHandler::handleRequest (java8) at http://127.0.0.1:3000/{proxy+} [OPTIONS GET HEAD POST PUT DELETE PATCH]
...
```

Using a new shell, you can send a test ping request to your API:

```bash
$ curl -s http://127.0.0.1:3000/ping | python -m json.tool

{
    "pong": true
}
```

or, with HTTPIE

```bash
$ http :3000/ping

HTTP/1.0 200 OK
Content-Length: 13
Content-Type: application/json
Date: Wed, 25 Sep 2019 00:50:54 GMT
Server: Werkzeug/0.15.6 Python/3.7.4

{
    "pong": true
}

```

You can use the [AWS CLI](https://aws.amazon.com/cli/) to quickly deploy your application to AWS Lambda and Amazon API Gateway with your SAM template.

You will need an S3 bucket to store the artifacts for deployment. Once you have created the S3 bucket, run the following command from the project's root folder - where the `sam.yaml` file is located:

```
$ aws cloudformation package --template-file sam.yaml --output-template-file output-sam.yaml --s3-bucket <YOUR S3 BUCKET NAME>
Uploading to xxxxxxxxxxxxxxxxxxxxxxxxxx  6464692 / 6464692.0  (100.00%)
Successfully packaged artifacts and wrote output template to file output-sam.yaml.
Execute the following command to deploy the packaged template
aws cloudformation deploy --template-file /your/path/output-sam.yaml --stack-name <YOUR STACK NAME>
```

As the command output suggests, you can now use the cli to deploy the application. Choose a stack name and run the `aws cloudformation deploy` command from the output of the package command.

```
$ aws cloudformation deploy --template-file output-sam.yaml --stack-name ServerlessMicronautApi --capabilities CAPABILITY_IAM
```

Once the application is deployed, you can describe the stack to show the API endpoint that was created. The endpoint should be the `ServerlessMicronautApi` key of the `Outputs` property:

```
$ aws cloudformation describe-stacks --stack-name ServerlessMicronautApi
{
    "Stacks": [
        {
            "StackId": "arn:aws:cloudformation:us-west-2:xxxxxxxx:stack/ServerlessMicronautApi/xxxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxx",
            "Description": "AWS Serverless Micronaut API - my.service::my-service",
            "Tags": [],
            "Outputs": [
                {
                    "Description": "URL for application",
                    "ExportName": "MyServiceApi",
                    "OutputKey": "MyServiceApi",
                    "OutputValue": "https://xxxxxxx.execute-api.us-west-2.amazonaws.com/ping"
                }
            ],
            "CreationTime": "2016-12-13T22:59:31.552Z",
            "Capabilities": [
                "CAPABILITY_IAM"
            ],
            "StackName": "ServerlessMicronautApi",
            "NotificationARNs": [],
            "StackStatus": "UPDATE_COMPLETE"
        }
    ]
}

```

Copy the `OutputValue`, adding the prefix `/Prod` to the path into a browser or use curl to test your first request:

```bash
$ curl -s https://xxxxxxx.execute-api.us-west-2.amazonaws.com/Prod/ping | python -m json.tool

{
    "pong": true
}
```
