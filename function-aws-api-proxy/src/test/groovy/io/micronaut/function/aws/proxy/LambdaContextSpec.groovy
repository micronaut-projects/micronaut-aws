package io.micronaut.function.aws.proxy

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import io.micronaut.aws.lambda.events.APIGatewayProxyRequestEvent
import io.micronaut.aws.lambda.events.APIGatewayProxyResponseEvent
import io.micronaut.aws.lambda.events.APIGatewayV2HTTPEvent
import io.micronaut.aws.lambda.events.APIGatewayV2HTTPResponse
import io.micronaut.aws.lambda.events.ApplicationLoadBalancerRequestEvent
import io.micronaut.aws.lambda.events.ApplicationLoadBalancerResponseEvent
import io.micronaut.function.aws.proxy.alb.ApplicationLoadBalancerFunction
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.Any
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.CollectionUtils
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

class LambdaContextSpec extends Specification {

    void "for v1 Lambda Context beans are registered"() {
        given:
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(ctxBuilder.build())

        when:
        APIGatewayProxyRequestEvent request = v1Request("/context")
        Context context = createContext()
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    void "for v2 Lambda Context beans are registered"() {
        given:
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        APIGatewayV2HTTPEventFunction handler =
                new APIGatewayV2HTTPEventFunction(ctxBuilder.build())

        when:
        APIGatewayV2HTTPEvent request = v2Request("/context")
        Context context = createContext()
        APIGatewayV2HTTPResponse response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    void "for application load balancer Lambda Context beans are registered"() {
        given:
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        ApplicationLoadBalancerFunction handler =
                new ApplicationLoadBalancerFunction(ctxBuilder.build())

        when:
        ApplicationLoadBalancerRequestEvent request = applicationLoadBalancerRequest("/context", HttpMethod.GET)
        Context context = createContext()
        ApplicationLoadBalancerResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    void "v2 verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(ctxBuilder.build())

        when:
        APIGatewayProxyRequestEvent request = v1Request("/context")
        Context context = createContextWithoutCollaborators()
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        and: 'LambdaLogger is not registered if Lambda Context::getLambdaLogger is null'
        !handler.applicationContext.containsBean(LambdaLogger)

        and: 'CognitoIdentity is not registered if Lambda Context::getIdentity is null"'
        !handler.applicationContext.containsBean(CognitoIdentity)

        and: 'ClientContext is not registered if Lambda Context::getClientContext is null"'
        !handler.applicationContext.containsBean(ClientContext)

        and:
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    void "v2 verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        APIGatewayV2HTTPEventFunction handler =
                new APIGatewayV2HTTPEventFunction(ctxBuilder.build())

        when:
        APIGatewayV2HTTPEvent request = v2Request("/context")
        Context context = createContextWithoutCollaborators()
        APIGatewayV2HTTPResponse response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        and: 'LambdaLogger is not registered if Lambda Context::getLambdaLogger is null'
        !handler.applicationContext.containsBean(LambdaLogger)

        and: 'CognitoIdentity is not registered if Lambda Context::getIdentity is null"'
        !handler.applicationContext.containsBean(CognitoIdentity)

        and: 'ClientContext is not registered if Lambda Context::getClientContext is null"'
        !handler.applicationContext.containsBean(ClientContext)

        and:
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    void "applicationLoadBalancer verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        Map<String, Object> properties = CollectionUtils.mapOf('micronaut.security.enabled', false, "spec.name", "LambdaContextSpec")
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(properties)
        ApplicationLoadBalancerFunction handler =
                new ApplicationLoadBalancerFunction(ctxBuilder.build())

        when:
        ApplicationLoadBalancerRequestEvent request = applicationLoadBalancerRequest("/context", HttpMethod.GET)
        Context context = createContextWithoutCollaborators()
        ApplicationLoadBalancerResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(Context)
        and: 'LambdaLogger is not registered if Lambda Context::getLambdaLogger is null'
        !handler.applicationContext.containsBean(LambdaLogger)

        and: 'CognitoIdentity is not registered if Lambda Context::getIdentity is null"'
        !handler.applicationContext.containsBean(CognitoIdentity)

        and: 'ClientContext is not registered if Lambda Context::getClientContext is null"'
        !handler.applicationContext.containsBean(ClientContext)

        and:
        "XXX" == response.body

        cleanup:
        handler.close()
    }

    static interface RequestIdProvider {
        @NonNull
        Optional<String> requestId();
    }

    @Requires(property = "spec.name", value = "LambdaContextSpec")
    @Singleton
    static class DefaultRequestIdProvider implements RequestIdProvider {
        private final BeanProvider<Context> context

        DefaultRequestIdProvider(@Any BeanProvider<Context> context) {
            this.context = context
        }

        @Override
        @NonNull
        Optional<String> requestId() {
            context.isPresent() ? Optional.of(context.get().awsRequestId) : Optional.empty()
        }
    }

    @Requires(property = "spec.name", value = "LambdaContextSpec")
    @Controller("/context")
    static class LambdaContextSpecController {
        @Inject
        RequestIdProvider requestIdProvider

        @Produces(MediaType.TEXT_PLAIN)
        @Get
        String index() {
            requestIdProvider.requestId().orElse("not found")
        }
    }

    Context createContextWithoutCollaborators() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> null
            getClientContext() >> null
            getClientContext() >> null
            getLogger() >> null
        }
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

    private static ApplicationLoadBalancerRequestEvent applicationLoadBalancerRequest(String path, HttpMethod httpMethod) {
        ApplicationLoadBalancerRequestEvent requestEvent = new ApplicationLoadBalancerRequestEvent();
        requestEvent.setPath(path)
        requestEvent.setHttpMethod(httpMethod.toString())
        requestEvent
    }
}
