package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification

class BodyArgumentsSpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
                    'spec.name': 'BodyArgumentsSpec'
            ])
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1164")
    void "test body arguments"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/body-arguments-test/getA', HttpMethod.POST.toString())
        builder.body('{"a":"A","b":"B"}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == 'A'
    }

    @Controller('/body-arguments-test')
    @Requires(property = 'spec.name', value = 'BodyArgumentsSpec')
    static class BodyController {

        @Post(uri = "/getA")
        @Produces(MediaType.TEXT_PLAIN)
        String getA(String a) {
            return a
        }
    }
}
