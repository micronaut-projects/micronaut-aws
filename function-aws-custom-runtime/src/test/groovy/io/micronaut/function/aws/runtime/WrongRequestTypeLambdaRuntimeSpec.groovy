package io.micronaut.function.aws.runtime

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.docs.Car
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class WrongRequestTypeLambdaRuntimeSpec extends Specification {

    void "invocation error endpoint of runtime api is called if wrong type is received"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, ['spec.name': 'WrongRequestTypeLambdaRuntimeSpec'])
        String serverUrl = "localhost:$embeddedServer.port"
        CustomAwsProxyEventMicronautLambdaRuntime customMicronautLambdaRuntime = new CustomAwsProxyEventMicronautLambdaRuntime(serverUrl)
        Thread t = new Thread({ ->
            customMicronautLambdaRuntime.run([] as String[])
        })
        t.start()

        MockLambdaRuntimeApi lambadaRuntimeApi = embeddedServer.applicationContext.getBean(MockLambdaRuntimeApi)

        expect:
        new PollingConditions(timeout: 5).eventually {
            assert lambadaRuntimeApi.errors
            assert lambadaRuntimeApi.errors['123456']
            assert  lambadaRuntimeApi.errors['123456'].errorMessage.contains('Unconvertible input')
        }

        cleanup:
        embeddedServer.close()
    }

    @Requires(property = 'spec.name', value = 'WrongRequestTypeLambdaRuntimeSpec')
    @Controller("/")
    static class MockLambdaRuntimeApi {

        Map<String, AwsLambdaRuntimeApiError> errors = [:]

        @Get("/2018-06-01/runtime/invocation/next")
        HttpResponse next() {
            Car car = new Car()
            car.cylinders = 8
            HttpResponse.ok(car)
                .header(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_AWS_REQUEST_ID, "123456")
        }

        @Post("/2018-06-01/runtime/invocation/{requestId}/error")
        HttpResponse<?> invocationError(@PathVariable("requestId") String requestId, @Body AwsLambdaRuntimeApiError runtimeApiError) {
            errors[requestId] = runtimeApiError
            return HttpResponse.accepted()
        }
    }

}
