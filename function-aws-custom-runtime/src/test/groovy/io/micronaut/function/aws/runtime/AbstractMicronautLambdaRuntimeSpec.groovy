package io.micronaut.function.aws.runtime

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.exceptions.ConfigurationException
import spock.lang.Specification

class AbstractMicronautLambdaRuntimeSpec extends Specification {

    void "validateHandler fails if handler is null"() {
        given:
        CustomRuntime customRuntime = new CustomRuntime()

        when:
        customRuntime.validateHandler()

        then:
        thrown(ConfigurationException)
    }
    
    static class CustomRuntime extends AbstractMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    }
}



