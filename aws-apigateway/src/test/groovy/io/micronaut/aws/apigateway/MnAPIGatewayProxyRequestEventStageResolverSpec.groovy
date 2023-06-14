package io.micronaut.aws.apigateway

import io.micronaut.aws.lambda.events.APIGatewayProxyRequestEvent
import spock.lang.Specification

class MnAPIGatewayProxyRequestEventStageResolverSpec extends Specification {

    void "resolve stage from APIGatewayProxyRequestEvent"() {
        given:
        MnAPIGatewayProxyRequestEventStageResolver resolver = new MnAPIGatewayProxyRequestEventStageResolver()
        def context = Stub(APIGatewayProxyRequestEvent.ProxyRequestContext) {
            getStage() >> 'foo'
        }
        def request = Stub(APIGatewayProxyRequestEvent)
        request.getRequestContext() >> context

        expect:
        'foo' == resolver.resolve(request).get()
    }

}
