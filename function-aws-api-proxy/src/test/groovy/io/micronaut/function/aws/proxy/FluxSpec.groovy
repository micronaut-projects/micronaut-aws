package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpMethod
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class FluxSpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test getAll method"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder("/users", HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '["Joe","Lewis"]'
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller("/users")
    static class UserController {

        @Get
        Flux<String> getAll() {
            return Flux.fromIterable(["Joe", "Lewis"])
        }

    }
}
