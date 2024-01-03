package io.micronaut.function.aws.runtime.micronautrequeststreamhandler

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.function.aws.MicronautRequestHandler
import io.micronaut.function.aws.runtime.AbstractRequestStreamHandlerMicronautLambdaRuntime
import spock.lang.Specification

class AbstractRequestStreamHandlerMicronautLambdaRuntimeSpec extends Specification {

    void "validateHandler fails if RequestHandler"() {
        given:
        CustomRuntime customRuntime = new CustomRuntime()

        when:
        customRuntime.validateHandler()

        then:
        thrown(ConfigurationException)
    }

    static class CustomRuntime extends AbstractRequestStreamHandlerMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

        @Override
        protected Object createHandler(String... args) {
            new FooHandler()
        }
    }

    static class FooHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

        @Override
        APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
            null
        }
    }
}



