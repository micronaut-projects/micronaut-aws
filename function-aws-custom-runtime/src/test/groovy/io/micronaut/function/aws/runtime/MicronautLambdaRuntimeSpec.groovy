package io.micronaut.function.aws.runtime

import com.amazonaws.serverless.proxy.model.ApiGatewayRequestIdentity
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification

class MicronautLambdaRuntimeSpec extends Specification {

    void "test runtime API loop"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
        boolean looped = false
        MicronautLambdaRuntime.startRuntimeApiEventLoop(
                embeddedServer.getURL(),
                ApplicationContext.build(),
                { URL ->
                    if (!looped) {
                        looped = true
                        return true
                    }
                    return false
                }
        )

        MockLambadaRuntimeApi lambadaRuntimeApi= embeddedServer.applicationContext.getBean(MockLambadaRuntimeApi)

        expect:
        lambadaRuntimeApi.responses
        lambadaRuntimeApi.responses['123456']
        lambadaRuntimeApi.responses['123456'].body == 'Hello 123456'


        cleanup:
        embeddedServer.close()
    }

    @Controller("/hello")
    static class HelloController {

        @Get("/world")
        String index(AwsProxyRequest request) {
            return "Hello " + request.getRequestContext().getRequestId()
        }
    }

    @Controller("/")
    static class MockLambadaRuntimeApi {

        Map<String, AwsProxyResponse> responses = [:]

        @Get("/2018-06-01/runtime/invocation/next")
        HttpResponse<AwsProxyRequest> next() {
            def req = new AwsProxyRequest()
            req.setPath('/hello/world')
            req.setHttpMethod("GET")
            def context = new AwsProxyRequestContext()
            context.setRequestId("123456")
            context.setIdentity(new ApiGatewayRequestIdentity())
            req.setRequestContext(context)
            HttpResponse.ok(req)
                .header(MicronautLambdaRuntime.HEADER_RUNTIME_AWS_REQUEST_ID, "123456")
        }

        @Post("/2018-06-01/runtime/invocation/{requestId}/response")
        HttpResponse<?> response(@PathVariable("requestId") String requestId, @Body AwsProxyResponse proxyResponse) {
            responses[requestId] = proxyResponse
            return HttpResponse.accepted()
        }
    }

}
