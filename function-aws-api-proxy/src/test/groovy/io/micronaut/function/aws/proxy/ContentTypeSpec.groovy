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
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification

class ContentTypeSpec extends Specification {
    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'spec.name'                 : 'ContentTypeSpec',
                    'micronaut.security.enabled': false,
            ])
    )
    @Shared
    Context lambdaContext = new MockLambdaContext()

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1330")
    @PendingFeature
    void "verify controllers return json by default"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/json/bydefault', HttpMethod.GET.toString())
        builder.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)

        when:
        AwsProxyResponse response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '{"msg":"Hello world"}'
        response.headers
        "application/json" == response.getHeaders().get("Content-Type")
    }

    @Controller('/json')
    @Requires(property = 'spec.name', value = 'ContentTypeSpec')
    static class BodyController {

        @Get("/bydefault")
        @Status(HttpStatus.OK)
        Map<String, Object> index() {
            [msg: "Hello world"]
        }
    }
}
