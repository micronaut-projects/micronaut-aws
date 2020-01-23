package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.validation.Validated
import org.reactivestreams.Publisher
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton
import javax.validation.Valid

class FiltersSpec extends Specification {
    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test filters are run correctly"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/filter-test/ok', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'OK'
        response.multiValueHeaders
        response.multiValueHeaders.getFirst('X-Test-Filter') == 'true'
    }

    void "test filters are run correctly with custom exception handler"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/filter-test/exception', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'Exception Handled'
        response.multiValueHeaders
        response.multiValueHeaders.getFirst('X-Test-Filter') == 'true'
    }

    @Controller("/filter-test")
    @Validated
    static class TestController {
        @Get("/ok")
        String ok() {
            return "OK"
        }

        @Get("/exception")
        void exception() {
            throw new CustomException()
        }

    }

    @Filter("/filter-test/**")
    static class TestFilter implements HttpServerFilter {
        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            return Publishers.map(chain.proceed(request), { mutableHttpResponse ->
                mutableHttpResponse.getHeaders().add("X-Test-Filter", "true")
                return mutableHttpResponse
            })
        }

    }

    static class CustomException extends RuntimeException {
    }

    @Produces
    @Singleton
    static class CustomExceptionHandler implements ExceptionHandler<CustomException, HttpResponse> {
        @Override
        HttpResponse handle(HttpRequest request, CustomException exception) {
            return HttpResponse.ok("Exception Handled")
        }
    }
}