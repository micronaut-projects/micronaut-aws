package io.micronaut.function.aws.runtime

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.Any
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class RuntimeApiSpec extends Specification {

    void "test runtime API loop"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'RuntimeApiSpec'])
        String serverUrl = "localhost:$embeddedServer.port"
        CustomMicronautLambdaRuntime customMicronautLambdaRuntime = new CustomMicronautLambdaRuntime(serverUrl)
        Thread t = new Thread({ ->
            customMicronautLambdaRuntime.run([] as String[])
        })
        t.start()

        MockLambadaRuntimeApi lambadaRuntimeApi = embeddedServer.applicationContext.getBean(MockLambadaRuntimeApi)

        expect:
        new PollingConditions(timeout: 5).eventually {
            assert lambadaRuntimeApi.responses
            assert lambadaRuntimeApi.responses['123456']
            assert lambadaRuntimeApi.responses['123456'].body == "Hello 123456"

            assert lambadaRuntimeApi.responses['78910']
            assert lambadaRuntimeApi.responses['78910'].body == "Hello 78910"
        }

        cleanup:
        embeddedServer.close()
    }

    class CustomMicronautLambdaRuntime extends MicronautLambdaRuntime {

        String serverUrl

        CustomMicronautLambdaRuntime(String serverUrl) {
            super()
            this.serverUrl = serverUrl
        }

        @Override
        String getEnv(String name) {
            if (name == ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API) {
                return serverUrl
            }
        }
    }

    @Controller("/hello")
    static class HelloController {
        @Any BeanProvider<Context> context

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/world")
        String index() {
            return "Hello " + context.get().awsRequestId
        }
    }

    @Requires(property = 'spec.name', value = 'RuntimeApiSpec')
    @Controller("/")
    static class MockLambadaRuntimeApi {

        Map<String, APIGatewayProxyResponseEvent> responses = [:]
        List<APIGatewayProxyRequestEvent> requests = [createRequest("123456"), createRequest("78910")]
        int count = 0;

        @Get("/2018-06-01/runtime/invocation/next")
        HttpResponse<APIGatewayProxyRequestEvent> next() {
            APIGatewayProxyRequestEvent req = requests.get(count++)
            HttpResponse.ok(req)
                    .header(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_AWS_REQUEST_ID, req.getRequestContext().getRequestId())
        }

        static APIGatewayProxyRequestEvent createRequest(String requestId) {
            APIGatewayProxyRequestEvent req = new APIGatewayProxyRequestEvent()
            req.setPath('/hello/world')
            req.setHttpMethod("GET")
            APIGatewayProxyRequestEvent.ProxyRequestContext context = new APIGatewayProxyRequestEvent.ProxyRequestContext()
            context.setRequestId(requestId)
            context.setIdentity(new APIGatewayProxyRequestEvent.RequestIdentity())
            req.setRequestContext(context)
            req
        }

        @Post("/2018-06-01/runtime/invocation/{requestId}/response")
        HttpResponse<?> response(@PathVariable("requestId") String requestId, @Body APIGatewayProxyResponseEvent proxyResponse) {
            responses[requestId] = proxyResponse
            return HttpResponse.accepted()
        }
    }
}
