package io.micronaut.function.aws.proxy

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.CollectionUtils
import io.micronaut.function.aws.proxy.alb.ApplicationLoadBalancerFunction
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.views.ModelAndView
import spock.lang.Specification

class HandlebarsSpec extends Specification {

    void "test handlebars view rendering for v1"() {
        given:
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'HandlebarsSpec'
                ).build()
        )

        when:
        def response = handler.handleRequest(v1Request("/views/render-html"), createContext())

        then:
        response.statusCode == 200
        response.body.trim() == '<html>Hello Luke Skywalker</html>'

        cleanup:
        handler.close()
    }

    void "test handlebars view rendering for v2"() {
        given:
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'HandlebarsSpec'
                ).build()
        )

        when:
        def response = handler.handleRequest(v2Request("/views/render-html"), createContext())

        then:
        response.statusCode == 200
        response.body.trim() == '<html>Hello Luke Skywalker</html>'
    }

    void "test handlebars view rendering for alb"() {
        given:
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'HandlebarsSpec'
                ).build()
        )

        when:
        def response = handler.handleRequest(applicationLoadBalancerRequest("/views/render-html"), createContext())

        then:
        response.statusCode == 200
        response.body.trim() == '<html>Hello Luke Skywalker</html>'
    }

    Context createContext() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> Mock(CognitoIdentity)
            getClientContext() >> Mock(ClientContext)
            getClientContext() >> Mock(ClientContext)
            getLogger() >> Mock(LambdaLogger)
        }
    }

    private static APIGatewayProxyRequestEvent v1Request(String path, HttpMethod method = HttpMethod.GET) {
        new APIGatewayProxyRequestEvent().withPath(path).withHttpMethod(method.toString())
    }

    private static APIGatewayV2HTTPEvent v2Request(String path, HttpMethod method = HttpMethod.GET) {
        APIGatewayV2HTTPEvent.RequestContext.Http http = APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                .withMethod(method.toString())
                .withPath(path)
                .build()
        APIGatewayV2HTTPEvent.RequestContext requestContext = APIGatewayV2HTTPEvent.RequestContext.builder()
                .withHttp(http)
                .build()
        APIGatewayV2HTTPEvent.builder()
                .withRequestContext(requestContext)
                .build()
    }

    private static ApplicationLoadBalancerRequestEvent applicationLoadBalancerRequest(String path, HttpMethod httpMethod = HttpMethod.GET) {
        ApplicationLoadBalancerRequestEvent requestEvent = new ApplicationLoadBalancerRequestEvent();
        requestEvent.setPath(path)
        requestEvent.setHttpMethod(httpMethod.toString())
        requestEvent
    }

    @Controller('/views')
    @Requires(property = 'spec.name', value = 'HandlebarsSpec')
    static class BodyController {

        @Get("/render-html")
        @Produces(MediaType.TEXT_HTML)
        ModelAndView getHtmlFromRenderEngine() {
            ModelAndView mav = new ModelAndView("home", CollectionUtils.mapOf("firstname", "Luke", "lastname", "Skywalker"));
            mav;
        }
    }
}
