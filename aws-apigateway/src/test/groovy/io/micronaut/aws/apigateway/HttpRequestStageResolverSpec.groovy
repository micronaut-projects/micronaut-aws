package io.micronaut.aws.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.micronaut.servlet.http.ServletHttpRequest
import spock.lang.Specification

class HttpRequestStageResolverSpec extends Specification {

    void "resolve stage from HttpRequest"() {
        given:
        HttpRequestStageResolver resolver = new HttpRequestStageResolver()
        def requestContextStub = Stub(APIGatewayV2HTTPEvent.RequestContext) {
            getStage() >> 'foo'
        }
        def proxyRequestStub = Stub(APIGatewayV2HTTPEvent) {
            getRequestContext() >> requestContextStub
        }
        def request = Stub(ServletHttpRequest) {
            getNativeRequest() >> proxyRequestStub
        }
        expect:
        'foo' == resolver.resolve(request).get()
    }

    void "no npe if no context"() {
        HttpRequestStageResolver resolver = new HttpRequestStageResolver()
        def proxyRequestStub = Stub(APIGatewayV2HTTPEvent) {
            getRequestContext() >> null
        }
        def request = Stub(ServletHttpRequest) {
            getNativeRequest() >> proxyRequestStub
        }

        expect:
        resolver.resolve(request).isEmpty()
    }

    void "resolve stage from HttpRequest"() {
        given:
        HttpRequestStageResolver resolver = new HttpRequestStageResolver()
        def requestContextStub = Stub(APIGatewayProxyRequestEvent.ProxyRequestContext) {
            getStage() >> 'foo'
        }
        def proxyRequestStub = Stub(APIGatewayProxyRequestEvent) {
            getRequestContext() >> requestContextStub
        }
        def request = Stub(ServletHttpRequest) {
            getNativeRequest() >> proxyRequestStub
        }
        expect:
        'foo' == resolver.resolve(request).get()
    }

}
