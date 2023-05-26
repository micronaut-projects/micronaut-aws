package io.micronaut.function.aws.runtime

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class CustomRuntimeForMicronautRequestHandlerSpec extends Specification {

    void "test runtime API loop"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'CustomRuntimeForMicronautRequestHandlerSpec'])
        String serverUrl = "localhost:$embeddedServer.port"

        CustomAwsProxyEventMicronautLambdaRuntime customMicronautLambdaRuntime = new CustomAwsProxyEventMicronautLambdaRuntime(serverUrl)
        Thread t = new Thread({ ->
            customMicronautLambdaRuntime.run([] as String[])
        })
        t.start()

        MockLambadaRuntimeApi lambadaRuntimeApi = embeddedServer.applicationContext.getBean(MockLambadaRuntimeApi)

        expect:
        new PollingConditions(timeout: 5).eventually {
            assert lambadaRuntimeApi.responses
            assert lambadaRuntimeApi.responses['123456']
            assert lambadaRuntimeApi.responses['123456'].body == '{"name":"Building Microservices","isbn":"XXX"}'.bytes.encodeBase64().toString()
        }

        cleanup:
        embeddedServer.close()
    }


    @Requires(property = 'spec.name', value = 'CustomRuntimeForMicronautRequestHandlerSpec')
    @Controller("/")
    static class MockLambadaRuntimeApi {

        Map<String, APIGatewayProxyResponseEvent> responses = [:]

        @Get("/2018-06-01/runtime/invocation/next")
        HttpResponse<APIGatewayProxyRequestEvent> next() {
            APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
            event.body = '{"name":"Building Microservices"}'
            HttpResponse.ok(event)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_AWS_REQUEST_ID, "123456")
        }

        @Post("/2018-06-01/runtime/invocation/{requestId}/response")
        HttpResponse<?> response(@PathVariable("requestId") String requestId, @Body APIGatewayProxyResponseEvent proxyResponse) {
            responses[requestId] = proxyResponse
            return HttpResponse.accepted()
        }
    }
}
