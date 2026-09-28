package io.micronaut.docs.aws.alexa.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Request
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Response
import com.amazon.ask.model.ui.SimpleCard
import com.amazon.ask.model.ui.SsmlOutputSpeech
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class HelloWorldIntentHandlerSpec extends Specification {

    @Inject
    HelloWorldIntentHandler handler

    void "handles the HelloWorld intent"() {
        given:
        HandlerInput input = handlerInput(IntentRequest.builder()
                .withIntent(Intent.builder().withName("HelloWorldIntent").build())
                .build())

        expect:
        handler.canHandle(input)
        !handler.canHandle(handlerInput(LaunchRequest.builder().build()))

        when:
        Optional<Response> response = handler.handle(input)

        then:
        response.present
        response.get().outputSpeech instanceof SsmlOutputSpeech
        ((SsmlOutputSpeech) response.get().outputSpeech).ssml == "<speak>Hello world</speak>"
        response.get().card instanceof SimpleCard
        ((SimpleCard) response.get().card).title == "HelloWorld"
        ((SimpleCard) response.get().card).content == "Hello world"
    }

    private static HandlerInput handlerInput(Request request) {
        HandlerInput.builder()
                .withRequestEnvelope(RequestEnvelope.builder().withRequest(request).build())
                .build()
    }
}
