package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ParametersSpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'spec.name': 'ParametersSpec'
            ])
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test getAll method"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/parameters-test/all', HttpMethod.GET.toString())
        builder.queryString("test", "one")
        builder.queryString("test", "two")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '["one","two"]'
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/parameters-test')
    @Requires(property = 'spec.name', value = 'ParametersSpec')
    static class BodyController {

        @Get(uri = "/all")
        List<String> all(HttpRequest request) {
            return request.getParameters().getAll("test")
        }
    }
}
