package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.LambdaClient

class LambdaClientSpec  extends ServiceClientSpec<LambdaClient, LambdaAsyncClient> {
    @Override
    protected String serviceName() {
        return LambdaClient.SERVICE_NAME
    }

    @Override
    protected LambdaClient getClient() {
        applicationContext.getBean(LambdaClient)
    }

    protected LambdaAsyncClient getAsyncClient() {
        applicationContext.getBean(LambdaAsyncClient)
    }
}
