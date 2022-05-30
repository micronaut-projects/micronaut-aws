package io.micronaut.function.aws.proxy


import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class MicronautLambdaHandlerStatusSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaHandler handler = new MicronautLambdaHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
            'spec.name': 'MicronautLambdaHandlerStatusSpec'
            ])
            )
    
    @Shared
    Context context = new MockLambdaContext()

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1387")
    void "test controller returning HttpStatus"(String path) {
        given:
        AwsProxyRequest input = new AwsProxyRequest();
        input.setPath(path)

        when:
        AwsProxyResponse response = handler.handleRequest(input, context)

        then:
        response.statusCode == 418

        where:
        path << ['/http-status', '/http-response-status', '/http-exception']
    }

    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerStatusSpec')
    @Controller('/http-status')
    static class HttpStatusController {

        @Get
        HttpStatus index() {
            HttpStatus.I_AM_A_TEAPOT
        }
    }

    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerStatusSpec')
    @Controller('/http-response-status')
    static class HttpResponseStatusController {

        @Get
        HttpResponse<?> index() {
            HttpResponse.status(HttpStatus.I_AM_A_TEAPOT)
        }
    }

    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerStatusSpec')
    @Controller('/http-exception')
    static class HttpResponseErrorController {

        @Get
        HttpResponse<?> index() {
            throw new TeapotException()
        }
    }

    static class TeapotException extends RuntimeException {
    }

    @Produces
    @Singleton
    static class TeapotExceptionHandler implements ExceptionHandler<TeapotException, HttpResponse<?>> {
        private final ErrorResponseProcessor<?> errorResponseProcessor

        TeapotExceptionHandler(ErrorResponseProcessor<?> errorResponseProcessor) {
            this.errorResponseProcessor = errorResponseProcessor
        }

        @Override
        HttpResponse<?> handle(HttpRequest request, TeapotException e) {
            errorResponseProcessor.processResponse(ErrorContext.builder(request)
                    .cause(e)
                    .build(), HttpResponse.status(HttpStatus.I_AM_A_TEAPOT))
        }
    }
}
