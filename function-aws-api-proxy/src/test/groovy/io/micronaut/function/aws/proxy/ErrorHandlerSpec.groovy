package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import spock.lang.AutoCleanup
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton
import javax.ws.rs.core.MediaType

class ErrorHandlerSpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = MicronautLambdaContainerHandler.getAwsProxyHandler(
            ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()


    void 'test custom global exception handlers'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/global', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.TEXT_PLAIN
        response.body == 'Exception Handled'
    }

    void 'test custom global exception handlers declared in controller'() {
        given:
            AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/global-ctrl', HttpMethod.GET.toString())

        when:
            def response = handler.proxy(builder.build(), lambdaContext)

        then:
            verifyAll {
                response.statusCode == 200
                response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.TEXT_PLAIN
                response.body == 'bad things happens globally'
            }
    }

    @PendingFeature
    void 'test custom global status handlers declared in controller'() {
        given:
            AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/global-status-ctrl', HttpMethod.GET.toString())

        when:
            def response = handler.proxy(builder.build(), lambdaContext)

        then:
            verifyAll {
                response.statusCode == 200
                response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.TEXT_PLAIN
                response.body == 'global status'
            }
    }

    void 'test local exception handlers'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/local', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'bad things'
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.TEXT_PLAIN
    }

    @Controller('/errors')
    static class ErrorController {

        @Get('/global')
        String globalHandler() {
            throw new MyException("bad things")
        }

        @Get('/global-ctrl')
        String globalControllerHandler() {
            throw new GloballyHandledException("bad things happens globally")
        }

        @Get('/global-status-ctrl')
        @Status(HttpStatus.I_AM_A_TEAPOT)
        String globalControllerHandlerForStatus() {
            return 'original global status'
        }

        @Get('/local')
        String localHandler() {
            throw new AnotherException("bad things")
        }

        @Error
        @Produces(io.micronaut.http.MediaType.TEXT_PLAIN)
        String localHandler(AnotherException throwable) {
            return throwable.getMessage()
        }

    }

    @Controller('/global-errors')
    static class GlobalErrorController {

        @Error(global = true, exception = GloballyHandledException)
        @Produces(io.micronaut.http.MediaType.TEXT_PLAIN)
        String globallyHandledException(GloballyHandledException throwable) {
            return throwable.getMessage()
        }

        @Error(global = true, status = HttpStatus.I_AM_A_TEAPOT)
        @Produces(io.micronaut.http.MediaType.TEXT_PLAIN)
        String globalControllerHandlerForStatus() {
            return 'global status'
        }

    }

    @Singleton
    static class MyErrorHandler implements io.micronaut.http.server.exceptions.ExceptionHandler<MyException, HttpResponse> {

        @Override
        HttpResponse handle(HttpRequest request, MyException exception) {
            return HttpResponse.ok("Exception Handled")
                               .contentType(MediaType.TEXT_PLAIN)
        }
    }

    @InheritConstructors
    static class MyException extends RuntimeException {
    }

    @InheritConstructors
    static class AnotherException extends RuntimeException {
    }

    @InheritConstructors
    static class GloballyHandledException extends Exception {
    }
}
