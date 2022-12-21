package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import spock.lang.Specification

import javax.validation.ConstraintViolationException

class ResponseStatusSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    void "test custom response status"() {
        given:

        def handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'micronaut.security.enabled': false,
                        'spec.name': 'ResponseStatusSpec'
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
        builder.body("foo")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == 'foo'

        cleanup:
        handler.close()
    }

    void "test multi status with alb"() {
        given:

        def handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'micronaut.security.enabled': false,
                        'spec.name': 'ResponseStatusSpec'
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status/multi-status', HttpMethod.GET.toString()).alb()
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 207

        cleanup:
        handler.close()
    }

    void "test optional causes 404"() {
        given:

        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'micronaut.security.enabled': false,
                        'spec.name': 'ResponseStatusSpec'
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status/optional', HttpMethod.GET.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 404

        cleanup:
        handler.close()
    }

    void "test null causes 404"() {
        given:
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'micronaut.security.enabled': false,
                        'spec.name': 'ResponseStatusSpec'
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status/null', HttpMethod.GET.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 404

        cleanup:
        handler.close()
    }

    void "test void methods does not cause 404"() {
        given:

        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'micronaut.security.enabled': false,
                        'spec.name': 'ResponseStatusSpec'
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status/delete-something', HttpMethod.DELETE.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 204

        cleanup:
        handler.close()
    }

    void "test constraint violation causes 400"() {
        given:

        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'spec.name': 'ResponseStatusSpec',
                        'micronaut.security.enabled':false
                ])
        )

        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-status/constraint-violation', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 400

        cleanup:
        handler.close()
    }

    @Controller('/response-status')
    @Requires(property = 'spec.name', value = 'ResponseStatusSpec')
    static class StatusController {

        @Post(uri = "/", processes = MediaType.TEXT_PLAIN)
        @Status(HttpStatus.CREATED)
        String post(@Body String data) {
            return data
        }

        @Get(uri = "/optional", processes = MediaType.TEXT_PLAIN)
        Optional<String> optional() {
            return Optional.empty()
        }

        @Get(uri = "/null", processes = MediaType.TEXT_PLAIN)
        String returnNull() {
            return null
        }

        @Post(uri = "/constraint-violation", processes = MediaType.TEXT_PLAIN)
        String constraintViolation() {
            throw new ConstraintViolationException("Failed", Collections.emptySet())
        }

        @Status(HttpStatus.NO_CONTENT)
        @Delete(uri = "/delete-something", processes = MediaType.TEXT_PLAIN)
        void deleteSomething() {
            // do nothing
        }

        @Get(uri = "/multi-status", processes = MediaType.TEXT_PLAIN)
        @Status(HttpStatus.MULTI_STATUS)
        void multiStatus() {
            // do nothing
        }
    }
}
