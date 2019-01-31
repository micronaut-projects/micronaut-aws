package io.micronaut.function.aws.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import spock.lang.Specification

import javax.inject.Singleton

class AlexaFunctionSpec extends Specification {

    void "test init"() {
        given:
        AlexaFunction function = new AlexaFunction()

        expect:
        AlexaFunction.currentAlexaApplicationContext.isRunning()

        cleanup:
        function?.close()
    }

    @Singleton
    static class MyHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput handlerInput) {
            return false
        }

        @Override
        Optional<Response> handle(HandlerInput handlerInput) {
            return Optional.empty()
        }
    }
}
