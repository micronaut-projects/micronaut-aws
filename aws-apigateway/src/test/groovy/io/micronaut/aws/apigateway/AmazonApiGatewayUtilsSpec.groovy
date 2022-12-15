package io.micronaut.aws.apigateway

import spock.lang.Specification

class AmazonApiGatewayUtilsSpec extends Specification {

    void "verify AmazonApiGateway"() {
        expect:
        AmazonApiGatewayUtils.isAmazonApiGatewayHost("https://xxx.execute-api.us-east-1.amazonaws.com")
        !AmazonApiGatewayUtils.isAmazonApiGatewayHost("https://micronaut.io")
    }
}
