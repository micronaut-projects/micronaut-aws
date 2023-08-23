package io.micronaut.function.aws.proxy

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.proxy.alb.ApplicationLoadBalancerFunction
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction
import io.micronaut.http.HttpMethod
import spock.lang.Specification

class StaticResourceSpec extends Specification {

    void "test static resources for v1"() {
        given:
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'StaticResourceSpec',
                        'micronaut.router.static-resources.swagger.paths': 'classpath:swagger',
                        'micronaut.router.static-resources.swagger.mapping': '/swagger/**',
                ).build()
        )

        when:
        def response = handler.handleRequest(v1Request("/swagger/application.yml"), createContext())
        def expected = StaticResourceSpec.getResourceAsStream("/swagger/application.yml").text.trim()

        then:
        response.statusCode == 200
        response.body.trim() == expected

        cleanup:
        handler.close()
    }

    void "test static resources for v2"() {
        given:
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'StaticResourceSpec',
                        'micronaut.router.static-resources.swagger.paths': 'classpath:swagger',
                        'micronaut.router.static-resources.swagger.mapping': '/swagger/**',
                ).build()
        )

        when:
        def response = handler.handleRequest(v2Request("/swagger/application.yml"), createContext())
        def expected = StaticResourceSpec.getResourceAsStream("/swagger/application.yml").text.trim()

        then:
        response.statusCode == 200
        response.body.trim() == expected

        cleanup:
        handler.close()
    }

    void "test static resources for ALB"() {
        given:
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction(
                ApplicationContext.builder().properties(
                        'micronaut.security.enabled': false,
                        'spec.name': 'StaticResourceSpec',
                        'micronaut.router.static-resources.swagger.paths': 'classpath:swagger',
                        'micronaut.router.static-resources.swagger.mapping': '/swagger/**',
                ).build()
        )

        when:
        def response = handler.handleRequest(applicationLoadBalancerRequest("/swagger/application.yml"), createContext())
        def expected = StaticResourceSpec.getResourceAsStream("/swagger/application.yml").text.trim()

        then:
        response.statusCode == 200
        response.body.trim() == expected

        cleanup:
        handler.close()
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
}
