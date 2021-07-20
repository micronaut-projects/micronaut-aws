package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.version.annotation.Version
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class VersionSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
        ApplicationContext.builder().properties([
                'micronaut.security.enabled': false,
                'spec.name'                                 : 'VersionSpec',
                'micronaut.router.versioning.enabled'       : true,
                'micronaut.router.versioning.header.enabled': true,
        ])
    )

    @Shared
    Context lambdaContext = new MockLambdaContext()

    void 'test controller method without version (default)'() {
        given:
        AwsProxyRequest request = new AwsProxyRequestBuilder('/version/ping', HttpMethod.GET.name())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .build()

        when:
        AwsProxyResponse response = handler.proxy(request, lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.getCode()
        response.body == 'pong v1'
    }

    void 'test controller method with version 2'() {
        given:
        AwsProxyRequest request = new AwsProxyRequestBuilder('/version/ping', HttpMethod.GET.name())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header('X-API-VERSION', '2')
            .build()

        when:
        AwsProxyResponse response = handler.proxy(request, lambdaContext)

        then:
        response.statusCode == HttpStatus.OK.getCode()
        response.body == 'pong v2'
    }

    @Controller('/version')
    @Requires(property = 'spec.name', value = 'VersionSpec')
    static class ConsumesController {

        @Get('/ping')
        String pingV1() {
            'pong v1'
        }

        @Version('2')
        @Get('/ping')
        String pingV2() {
            'pong v2'
        }
    }
}
