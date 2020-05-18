package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.JsonMappingException
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.AutoCleanup
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton
import javax.ws.rs.core.MediaType

class ErrorHandlerSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.build().properties([
                    'spec.name': 'ErrorHandlerSpec',
                    'micronaut.server.cors.enabled': true,
                    'micronaut.server.cors.configurations.web.allowedOrigins': ['http://localhost:8080']
            ])
    )
    @Shared
    Context lambdaContext = new MockLambdaContext()


    void 'test custom global exception handlers'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/global', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'Exception Handled'
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.TEXT_PLAIN
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

    void 'it can process JsonProcessingException'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/error', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 400
        response.body.contains 'Invalid JSON: invalid json'
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.APPLICATION_JSON
    }

    void 'cors headers are present after exceptions'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/error', HttpMethod.GET.toString())
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.multiValueHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == 'http://localhost:8080'
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/errors')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
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

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/json')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class JsonController {
        @Get('/error')
        String jsonException() {
            throw new JsonMappingException("invalid json")
        }
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/global-errors')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
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
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class RuntimeErrorHandler implements ExceptionHandler<RuntimeException, HttpResponse> {

        @Override
        HttpResponse handle(HttpRequest request, RuntimeException exception) {
            return HttpResponse.serverError("Exception: " + exception.getMessage())
                    .contentType(MediaType.TEXT_PLAIN)
        }
    }

    @Singleton
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class MyErrorHandler implements ExceptionHandler<MyException, HttpResponse> {

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
