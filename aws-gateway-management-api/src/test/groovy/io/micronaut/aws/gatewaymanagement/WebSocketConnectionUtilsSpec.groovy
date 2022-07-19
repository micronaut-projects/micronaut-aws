package io.micronaut.aws.gatewaymanagement

import spock.lang.Specification

class WebSocketConnectionUtilsSpec extends Specification {

    void "test UriBuilder" () {
        when:
        WebSocketConnection webSocketConnection = new WebSocketConnection("us-east-1",
                "ydvi4h9bvd",
                "production",
                "x94eGsoAMCLig=",
                "ydvi4h9bvd.execute-api.us-east-1.amazonaws.com")

        then:
        WebSocketConnectionUtils.uriOf(webSocketConnection).toString() ==
                "https://" + webSocketConnection.getDomainName() + "/production"
    }
}
