package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.http.server.exceptions.ExceptionHandler
import org.reactivestreams.Publisher
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton

import static io.micronaut.http.HttpMethod.GET

class RemoteAddressSpec extends Specification {
    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test remote address comes from identity sourceIp"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(
                '/remoteAddress/fromSourceIp', GET.toString());

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.multiValueHeaders
        response.multiValueHeaders.getFirst('X-Captured-Remote-Address') == '127.0.0.1'
    }

    @Controller("/remoteAddress")
    static class TestController {
        @Get("fromSourceIp")
        void sourceIp() {
        }
    }

    @Filter("/remoteAddress/**")
    static class CaptureRemoteAddressFiter implements HttpServerFilter {
        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            return Publishers.map(chain.proceed(request), { mutableHttpResponse ->
                mutableHttpResponse.getHeaders().add("X-Captured-Remote-Address",
                        request.getRemoteAddress().getAddress().getHostAddress())
                return mutableHttpResponse
            })
        }
    }

    @Produces
    @Singleton
    static class CustomExceptionHandler implements ExceptionHandler<Exception, HttpResponse> {
        @Override
        HttpResponse handle(HttpRequest request, Exception exception) {
            exception.printStackTrace(System.err);
            return HttpResponse.serverError(exception.toString())
        }
    }
}