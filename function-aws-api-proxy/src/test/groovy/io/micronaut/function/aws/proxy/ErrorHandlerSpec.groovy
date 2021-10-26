package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.http.codec.CodecException
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

import jakarta.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.Min
import javax.ws.rs.core.MediaType

class ErrorHandlerSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'spec.name': 'ErrorHandlerSpec',
                    'micronaut.server.cors.enabled': true,
                    'micronaut.server.cors.configurations.web.allowedOrigins': ['http://localhost:8080']
            ])
    )
    @Shared
    Context lambdaContext = new MockLambdaContext()

    @Shared
    ObjectMapper objectMapper = handler.getApplicationContext().getBean(ObjectMapper.class)

    void 'test custom global exception handlers for POST with body'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/errors/global', HttpMethod.POST.toString())
                .header(HttpHeaders.CONTENT_TYPE, io.micronaut.http.MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(new RequestObject(101)))

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body.startsWith("{\"message\":\"Error: bad things when post and body in request\",\"")
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.APPLICATION_JSON
    }

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

    void 'json message format errors return 400'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/jsonBody', HttpMethod.POST.toString()).body("{\"numberField\": \"textInsteadOfNumber\"}")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 400
        response.body.contains 'Invalid JSON: Error decoding JSON stream for type'
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.APPLICATION_JSON
    }

    void 'message validation errors return 400'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/jsonBody', HttpMethod.POST.toString()).body("{\"numberField\": 0}")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 400
        response.body.contains 'data.numberField: must be greater than or equal to 1'
        response.multiValueHeaders.getFirst(HttpHeaders.CONTENT_TYPE) == io.micronaut.http.MediaType.APPLICATION_JSON
    }

    void 'cors headers are present after exceptions'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/errors/global', HttpMethod.GET.toString())
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.multiValueHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == 'http://localhost:8080'
    }

    void 'cors headers are present after failed deserialisation'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/jsonBody', HttpMethod.POST.toString())
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .body('{"numberField": "string is not a number"}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.multiValueHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == 'http://localhost:8080'
    }

    void 'secured controller returns 401'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/secret', HttpMethod.GET.toString())
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)

        when:
        AwsProxyResponse response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 401
    }

    void 'cors headers are present after failed deserialisation when error handler is used'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/errors/global', HttpMethod.POST.toString())
                .header(HttpHeaders.ORIGIN, "http://localhost:8080")
                .body('{"numberField": "string is not a number"}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.multiValueHeaders.getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == 'http://localhost:8080'
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Controller('/secret')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class SecretController {
        @Get
        @Produces(MediaType.TEXT_PLAIN)
        String index() {
            "area 51 hosts an alien"
        }
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
        @Status(HttpStatus.OK)
        String localHandler(AnotherException throwable) {
            return throwable.getMessage()
        }
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Issue("issues/761")
    @Controller(value = '/json/errors', produces = io.micronaut.http.MediaType.APPLICATION_JSON)
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class JsonErrorController {

        @Post('/global')
        String globalHandlerPost(@Body RequestObject object) {
            throw new RuntimeException("bad things when post and body in request")
        }

        @Error
        HttpResponse<JsonError> errorHandler(HttpRequest request, RuntimeException exception) {
            JsonError error = new JsonError("Error: " + exception.getMessage())
                    .link(Link.SELF, Link.of(request.getUri()));

            return HttpResponse.<JsonError>status(HttpStatus.OK)
                    .body(error)
        }
    }

    @Introspected
    static class RequestObject {
        @Min(1L)
        Integer numberField;

        RequestObject(Integer numberField) {
            this.numberField = numberField
        }
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/json')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class JsonController {
        @Post('/jsonBody')
        String jsonBody(@Valid @Body RequestObject data) {
            return "blah"
        }
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/global-errors')
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class GlobalErrorController {

        @Error(global = true, exception = GloballyHandledException)
        @Produces(io.micronaut.http.MediaType.TEXT_PLAIN)
        @Status(HttpStatus.OK)
        String globallyHandledException(GloballyHandledException throwable) {
            return throwable.getMessage()
        }

        @Error(global = true, status = HttpStatus.I_AM_A_TEAPOT)
        @Produces(io.micronaut.http.MediaType.TEXT_PLAIN)
        @Status(HttpStatus.OK)
        String globalControllerHandlerForStatus() {
            return 'global status'
        }

    }

    @Singleton
    @Requires(property = 'spec.name', value = 'ErrorHandlerSpec')
    static class CodecExceptionExceptionHandler
            implements ExceptionHandler<CodecException, HttpResponse> {

        @Override
        HttpResponse handle(HttpRequest request, CodecException exception) {
            return HttpResponse.badRequest("Invalid JSON: " + exception.getMessage()).contentType(MediaType.APPLICATION_JSON)
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
