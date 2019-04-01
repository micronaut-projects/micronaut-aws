package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ConsumesSpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = MicronautLambdaContainerHandler.getAwsProxyHandler(
            ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()


    void "test multiple consumes definition"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/consumes-test', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"name":"Fred"}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '{"name":"Fred"}'

    }

    @Controller('/consumes-test')
    static class ConsumesController {

        @Post('/')
        @Consumes([MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON])
        Pojo save(@Body Pojo pojo) {
            pojo
        }
    }

    static class Pojo {
        String name
    }
}
