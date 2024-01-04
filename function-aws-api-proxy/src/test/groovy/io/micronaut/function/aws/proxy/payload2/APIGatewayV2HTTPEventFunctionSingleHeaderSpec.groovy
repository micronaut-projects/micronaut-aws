package io.micronaut.function.aws.proxy.payload2

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import spock.lang.Specification

class APIGatewayV2HTTPEventFunctionSingleHeaderSpec extends Specification {

    void "should retrieve and validate the header"() {
        given:
        ApplicationContext ctx = ApplicationContext.builder().properties('micronaut.security.enabled': false, 'spec.name': 'APIGatewayV2HTTPEventFunctionSingleHeaderSpec').build()
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)

        when:
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent()
        event.setHeaders(Map.of(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN, "sample-header", "HERE IT IS"))
        event.setRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder().withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder().withPath("/single-value-header").withMethod("GET").build()).build())

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, createContext())

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

    @Requires(property = "spec.name", value = 'APIGatewayV2HTTPEventFunctionSingleHeaderSpec')
    @Controller
    static class SampleController {

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/single-value-header")
        String singleValueHeader(HttpRequest<?> request) {
            return request.getHeaders().get("sample-header", String.class).orElse("HEADER NOT DEFINED");
        }
    }
}
