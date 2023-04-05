package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.model.SingleValueHeaders
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Status
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class SingleValueHeaderSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties([
                    'micronaut.security.enabled': false,
                    'spec.name': 'SingleValueHeaderSpec'
            ])
    )

    @Shared
    Context lambdaContext = new MockLambdaContext()

    void "test singleValuesHeaders"() {
        given:
        AwsProxyRequest request = new AwsProxyRequest();
        SingleValueHeaders headers = new SingleValueHeaders()
        headers.put("x-amzn-trace-id", "Root=1-62e22402-3a5f246225e45edd7735c182")
        headers.put(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
        request.setHeaders(headers)
        request.setHttpMethod("GET")
        request.setPath("/singleValueHeaders")

        when:
        AwsProxyResponse response = handler.proxy(request, lambdaContext)

        then:
        response.statusCode == 201
        response.getBody() == "Root=1-62e22402-3a5f246225e45edd7735c182"
    }

    @Controller('/singleValueHeaders')
    @Requires(property = 'spec.name', value = 'SingleValueHeaderSpec')
    static class SingleValueHeadersController {

        @Produces(MediaType.TEXT_PLAIN)
        @Get
        @Status(HttpStatus.CREATED)
        String singeHeaders(@Header("x-amzn-trace-id") String xAmznTraceId) {
            return xAmznTraceId
        }

    }
}
