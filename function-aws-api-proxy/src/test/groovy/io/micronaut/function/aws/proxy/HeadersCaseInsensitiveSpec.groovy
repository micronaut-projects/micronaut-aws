package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class HeadersCaseInsensitiveSpec extends Specification {
    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'spec.name'                 : 'HeadersCaseInsensitiveSpec',
                    'micronaut.security.enabled': false,
            ])
    )
    @Shared
    Context lambdaContext = new MockLambdaContext()

    @Unroll
    void "verify controllers return json by default and headers are populated"(String accept) {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/text', HttpMethod.GET.toString())
        builder.header(HttpHeaders.ACCEPT, accept)

        when:
        AwsProxyResponse response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'Hello World'

        response.multiValueHeaders
        ["text/plain"] == response.multiValueHeaders.get("Content-Type")

        where:
        accept << ["teXt/PlaiN", MediaType.TEXT_PLAIN]
    }

    @Controller('/json')
    static class BodyController {

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/text")
        String index() {
            "Hello World"
        }
    }
}
