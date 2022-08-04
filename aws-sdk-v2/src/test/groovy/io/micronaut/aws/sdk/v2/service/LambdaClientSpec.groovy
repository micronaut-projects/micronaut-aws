package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.LambdaClientBuilder

class LambdaClientSpec extends ApplicationContextSpecification {

    private static final String ENDPOINT = "https://localhost:1234"

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                "aws.lambda.endpoint-override": ENDPOINT
        ]
    }

    void "it can configure a sync client"() {
        when:
        LambdaClient client = applicationContext.getBean(LambdaClient)

        then:
        client.serviceName() == LambdaClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        LambdaAsyncClient client = applicationContext.getBean(LambdaAsyncClient)

        then:
        client.serviceName() == LambdaAsyncClient.SERVICE_NAME
    }

    void "it can override endpoint"() {
        when:
        LambdaClientBuilder builder = applicationContext.getBean(LambdaClientBuilder)

        then:
        builder.syncClientConfiguration().option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
