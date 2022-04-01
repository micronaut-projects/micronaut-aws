package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaderValues
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Status
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class DeleteWithoutBodySpec extends Specification {
    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
                    'spec.name': 'DeleteWithoutBodySpec'
            ])
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "verifies it is possible to exposes a delete endpoint which is invoked without a body"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/sessions/sergio', HttpMethod.DELETE.toString())
                .header(HttpHeaders.AUTHORIZATION, HttpHeaderValues.AUTHORIZATION_PREFIX_BEARER + " xxx")

        when:
        AwsProxyResponse response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
    }

    @Requires(property = 'spec.name', value = 'DeleteWithoutBodySpec')
    @Controller('/sessions')
    static class SessionsController {
        @Status(HttpStatus.OK)
        @Delete("/{username}")
        void delete(@PathVariable String username,
                    @Header(HttpHeaders.AUTHORIZATION) String authorization) {
        }
    }
}
