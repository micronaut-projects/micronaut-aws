package io.micronaut.aws.apigateway

import com.amazonaws.serverless.proxy.RequestReader
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext
import io.micronaut.http.HttpRequest
import spock.lang.Specification

class HttpRequestStageResolverSpec extends Specification {

    void "resolve stage from HttpRequest"() {
        given:
        HttpRequestStageResolver resolver = new HttpRequestStageResolver()
        def context = Stub(AwsProxyRequestContext) {
            getStage() >> 'foo'
        }
        def request = Stub(HttpRequest) {
            getAttribute(RequestReader.API_GATEWAY_CONTEXT_PROPERTY, AwsProxyRequestContext.class) >> Optional.of(context)
        }
        expect:
        'foo' == resolver.resolve(request).get()
    }

}
