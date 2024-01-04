package io.micronaut.function.aws.proxy.alb

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import spock.lang.Specification

class ApplicationLoadBalancerFunctionSingleHeaderSpec extends Specification {

    void "should retrieve and validate the header"() {
        given:
        ApplicationContext ctx = ApplicationContext.builder().properties('micronaut.security.enabled': false, 'spec.name': 'ApplicationLoadBalancerFunctionSingleHeaderSpec').build()
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction(ctx)

        ApplicationLoadBalancerRequestEvent request = new ApplicationLoadBalancerRequestEvent();
        request.setPath("/single-value-header")
        request.setHttpMethod("GET")
        request.setHeaders(Map.of(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN, "sample-header", "HERE IT IS"))

        when:
        ApplicationLoadBalancerResponseEvent response = handler.handleRequest(request, createContext())

        then:
        200 == response.statusCode
        'HERE IT IS' == response.body
    }

    private Context createContext() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> Mock(CognitoIdentity)
            getClientContext() >> Mock(ClientContext)
            getClientContext() >> Mock(ClientContext)
            getLogger() >> Mock(LambdaLogger)
        }
    }

    @Requires(property = "spec.name", value = 'ApplicationLoadBalancerFunctionSingleHeaderSpec')
    @Controller
    static class SampleController {

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/single-value-header")
        String singleValueHeader(HttpRequest<?> request) {
            return request.getHeaders().get("sample-header", String.class).orElse("HEADER NOT DEFINED");
        }
    }
}
