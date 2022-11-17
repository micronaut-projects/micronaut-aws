package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import jakarta.inject.Singleton

class StatusSpec  extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
                    'spec.name': 'StatusSpec'
            ])
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1387")
    void "test controller returning HttpStatus"(String path) {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 418

        where:
        path << ['/http-status', '/http-response-status', '/http-exception']
    }

    @Requires(property = 'spec.name', value = 'StatusSpec')
    @Controller('/http-status')
    static class HttpStatusController {

        @Get
        HttpStatus index() {
            HttpStatus.I_AM_A_TEAPOT
        }
    }

    @Requires(property = 'spec.name', value = 'StatusSpec')
    @Controller('/http-response-status')
    static class HttpResponseStatusController {

        @Get
        HttpResponse<?> index() {
            HttpResponse.status(HttpStatus.I_AM_A_TEAPOT)
        }
    }

    @Requires(property = 'spec.name', value = 'StatusSpec')
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
