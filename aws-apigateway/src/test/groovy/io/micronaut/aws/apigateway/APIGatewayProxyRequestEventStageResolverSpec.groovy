package io.micronaut.aws.apigateway

import spock.lang.Specification
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

class APIGatewayProxyRequestEventStageResolverSpec extends Specification {

    void "resolve stage from APIGatewayProxyRequestEvent"() {
        given:
        APIGatewayProxyRequestEventStageResolver resolver = new APIGatewayProxyRequestEventStageResolver()
        def context = Stub(APIGatewayProxyRequestEvent.ProxyRequestContext) {
            getStage() >> 'foo'
        }
        def request = Stub(APIGatewayProxyRequestEvent)
        request.getRequestContext() >> context

        expect:
        'foo' == resolver.resolve(request).get()
    }

}
