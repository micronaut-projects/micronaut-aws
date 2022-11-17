package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient

class ApiGatewayManagementApiClientSpec extends ApplicationContextSpecification {
    void "it can configure an API Gateway Management Api Client client"() {
        when:
        ApiGatewayManagementApiClient client = applicationContext.getBean(ApiGatewayManagementApiClient)

        then:
        client.serviceName() == ApiGatewayManagementApiClient.SERVICE_NAME
    }

    void "it can configure an API Gateway Management Api Client async client"() {
        when:
        ApiGatewayManagementApiAsyncClient client = applicationContext.getBean(ApiGatewayManagementApiAsyncClient)

        then:
        client.serviceName() == ApiGatewayManagementApiAsyncClient.SERVICE_NAME
    }
}
