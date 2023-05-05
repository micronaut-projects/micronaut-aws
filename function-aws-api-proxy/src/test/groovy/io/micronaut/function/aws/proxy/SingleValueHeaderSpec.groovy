package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.model.Headers
import com.amazonaws.serverless.proxy.model.SingleValueHeaders
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
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
import spock.lang.Unroll

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

    @Unroll
    void "test singleValuesHeaders"(String path) {
        given:

        String headerName = "x-amzn-trace-id"
        String headerValue = "Root=1-62e22402-3a5f246225e45edd7735c182"

        when:
        AwsProxyRequest request = proxyRequestWithSingleValueHeaders(path, headerName, headerValue)
        AwsProxyResponse response = handler.proxy(request, lambdaContext)

        then:
        response.statusCode == 201
        response.getBody() == headerValue

        when:
        request = proxyRequestWithMultiValueHeaders(path, headerName, headerValue)
        response = handler.proxy(request, lambdaContext)

        then:
        response.statusCode == 201
        response.getBody() == headerValue

        where:
        path << [
                 '/singleValueHeaders',
                 '/singleValueHeaders/request'
        ]
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

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/request")
        HttpResponse<String> index(HttpRequest<?> request) {
            String message = request.getHeaders().get("X-AMZN-TRACE-ID")
            HttpResponse.status(HttpStatus.CREATED).body(message)
        }
    }

    private static SingleValueHeaders singleValueHeaders(String headerName, String headerValue) {
        SingleValueHeaders headers = new SingleValueHeaders()
        headers.put(headerName, headerValue)
        headers.put(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
        headers
    }

    private static Headers multiValueHeaders(String headerName, String headerValue) {
        Headers headers = new Headers()
        headers.put(headerName,  Collections.singletonList(headerValue))
        headers.put(HttpHeaders.ACCEPT,  Collections.singletonList(MediaType.TEXT_PLAIN))
        headers
    }

    private static AwsProxyRequest proxyRequestWithMultiValueHeaders(String path,
                                                                     String headerName,
                                                                     String headerValue) {
        AwsProxyRequest request = new AwsProxyRequest()
        request.setHttpMethod("GET")
        request.setMultiValueHeaders(multiValueHeaders(headerName, headerValue))
        request.setPath(path)
        request
    }

    private static AwsProxyRequest proxyRequestWithSingleValueHeaders(String path,
                                                                      String headerName,
                                                                      String headerValue) {
        AwsProxyRequest request = new AwsProxyRequest()
        request.setHeaders(singleValueHeaders(headerName, headerValue))
        request.setHttpMethod("GET")
        request.setPath(path)
        request
    }
}
