package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

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

    void 'secured controller returns 401'() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/secret', HttpMethod.GET.toString())
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)

        when:
        AwsProxyResponse response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 401
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
}
