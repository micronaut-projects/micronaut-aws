package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import spock.lang.Specification

class ResponseStatusSpec extends Specification {

    private static Context lambdaContext = new MockLambdaContext()

    void "test custom response status"() {
        given:

        def handler = MicronautLambdaContainerHandler.getAwsProxyHandler(
                ApplicationContext.build()
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


    @Controller('/response-status')
    static class StatusController {

        @Post(uri = "/", processes = MediaType.TEXT_PLAIN)
        @Status(HttpStatus.CREATED)
        String post(@Body String data) {
            return data
        }
    }
}
