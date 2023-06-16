package io.micronaut.function.aws.proxy

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Requires
import io.micronaut.function.aws.proxy.alb.ApplicationLoadBalancerFunction
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BinaryContentConfigurationSpec extends Specification {

    void "test v1 zip is considered binary"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(ctxBuilder.build())

        when:
        APIGatewayProxyRequestEvent request = v1Request("/context")
        Context context = createContext()
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(BinaryContentConfiguration)
        response.isBase64Encoded

        when:
        def zis = new ZipInputStream(new ByteArrayInputStream(Base64.mimeDecoder.decode(response.body.getBytes())))

        then:
        with(zis.nextEntry) {
            name == 'test.txt'
            new String(zis.readAllBytes(), StandardCharsets.UTF_8) == 'test'
        }

        cleanup:
        handler.close()
    }

    void "test v2 binary content configuration"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctxBuilder.build())

        when:
        APIGatewayV2HTTPEvent request = v2Request("/context", HttpMethod.GET)
        Context context = createContext()
        APIGatewayV2HTTPResponse response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(BinaryContentConfiguration)
        response.isBase64Encoded

        when:
        def zis = new ZipInputStream(new ByteArrayInputStream(Base64.mimeDecoder.decode(response.body.getBytes())))

        then:
        with(zis.nextEntry) {
            name == 'test.txt'
            new String(zis.readAllBytes(), StandardCharsets.UTF_8) == 'test'
        }

        cleanup:
        handler.close()
    }

    void "test application load balancer binary content configuration"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction(ctxBuilder.build())

        when:
        ApplicationLoadBalancerRequestEvent request = applicationLoadBalancerRequest("/context", HttpMethod.GET)
        Context context = createContext()
        ApplicationLoadBalancerResponseEvent response = handler.handleRequest(request, context)

        then:
        handler.applicationContext.containsBean(BinaryContentConfiguration)
        response.isBase64Encoded

        when:
        def zis = new ZipInputStream(new ByteArrayInputStream(Base64.mimeDecoder.decode(response.body.getBytes())))

        then:
        with(zis.nextEntry) {
            name == 'test.txt'
            new String(zis.readAllBytes(), StandardCharsets.UTF_8) == 'test'
        }

        cleanup:
        handler.close()
    }

    void "test v1 binary content types can be updated"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        ApplicationContext ctx = ctxBuilder.build()
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(ctx)
        BinaryContentConfiguration binaryContentConfiguration = ctx.getBean(BinaryContentConfiguration)

        when:
        APIGatewayProxyRequestEvent request = v1Request("/context/plain")
        Context context = createContext()
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context)

        then:
        !response.isBase64Encoded
        response.body == 'ok'

        when:
        binaryContentConfiguration.addBinaryContentType(MediaType.TEXT_PLAIN)
        request = v1Request("/context/plain")
        response = handler.handleRequest(request, context)

        then:
        response.isBase64Encoded
        new String(Base64.mimeDecoder.decode(response.body.getBytes()), StandardCharsets.UTF_8) == 'ok'

        cleanup:
        handler.close()
    }

    void "test v2 binary content types can be updated"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        ApplicationContext ctx = ctxBuilder.build()
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)
        BinaryContentConfiguration binaryContentConfiguration = ctx.getBean(BinaryContentConfiguration)

        when:
        APIGatewayV2HTTPEvent request = v2Request("/context/plain", HttpMethod.GET)
        Context context = createContext()
        APIGatewayV2HTTPResponse response = handler.handleRequest(request, context)

        then:
        !response.isBase64Encoded
        response.body == 'ok'

        when:
        binaryContentConfiguration.addBinaryContentType(MediaType.TEXT_PLAIN)
        request = v2Request("/context/plain", HttpMethod.GET)
        response = handler.handleRequest(request, context)

        then:
        response.isBase64Encoded
        new String(Base64.mimeDecoder.decode(response.body.getBytes()), StandardCharsets.UTF_8) == 'ok'

        cleanup:
        handler.close()
    }

    void "test application loadbalancer binary content types can be updated"() {
        given:
        ApplicationContextBuilder ctxBuilder = ApplicationContext.builder().properties(
                'micronaut.security.enabled': false,
                'spec.name': 'BinaryContentConfigurationSpec'
        )
        ApplicationContext ctx = ctxBuilder.build()
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction(ctx)
        BinaryContentConfiguration binaryContentConfiguration = ctx.getBean(BinaryContentConfiguration)

        when:
        ApplicationLoadBalancerRequestEvent request = applicationLoadBalancerRequest("/context/plain", HttpMethod.GET)
        Context context = createContext()
        ApplicationLoadBalancerResponseEvent response = handler.handleRequest(request, context)

        then:
        !response.isBase64Encoded
        response.body == 'ok'

        when:
        binaryContentConfiguration.addBinaryContentType(MediaType.TEXT_PLAIN)
        request = applicationLoadBalancerRequest("/context/plain", HttpMethod.GET)
        response = handler.handleRequest(request, context)

        then:
        response.isBase64Encoded
        new String(Base64.mimeDecoder.decode(response.body.getBytes()), StandardCharsets.UTF_8) == 'ok'

        cleanup:
        handler.close()
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

    Context createContext() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> Mock(CognitoIdentity)
            getClientContext() >> Mock(ClientContext)
            getClientContext() >> Mock(ClientContext)
            getLogger() >> Mock(LambdaLogger)
        }
    }

    private static ApplicationLoadBalancerRequestEvent applicationLoadBalancerRequest(String path, HttpMethod httpMethod) {
        ApplicationLoadBalancerRequestEvent requestEvent = new ApplicationLoadBalancerRequestEvent();
        requestEvent.setPath(path)
        requestEvent.setHttpMethod(httpMethod.toString())
        requestEvent
    }

    @Requires(property = "spec.name", value = "BinaryContentConfigurationSpec")
    @Controller("/context")
    static class LambdaContextSpecController {

        @Get
        @Produces("application/zip")
        byte[] index() {
            def baos = new ByteArrayOutputStream()
            new ZipOutputStream(baos).with {
                it.putNextEntry(new ZipEntry("test.txt"))
                write("test".bytes)
                closeEntry()
                close()
                baos.toByteArray()
            }
        }

        @Get("/plain")
        @Produces(MediaType.TEXT_PLAIN)
        String plain() {
            "ok"
        }
    }
}
