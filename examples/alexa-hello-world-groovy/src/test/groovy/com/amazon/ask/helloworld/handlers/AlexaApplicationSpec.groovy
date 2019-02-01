package com.amazon.ask.helloworld.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Response
import com.amazon.ask.model.ui.OutputSpeech
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class AlexaApplicationSpec extends Specification {

    @Inject
    AlexaApplication handler

    @Inject
    RequestHandler[] requestHandlers

    void "test hello world intent"() throws Exception {
        when:"A intent request is built"
        final HandlerInput.Builder builder = HandlerInput.builder()
        final RequestEnvelope.Builder envelopeBuilder = RequestEnvelope.builder()
        def intentRequestBuilder = IntentRequest.builder()
        intentRequestBuilder.withIntent(Intent.builder().withName("HelloWorldIntent").build())
        envelopeBuilder.withRequest(intentRequestBuilder.build())
        builder.withRequestEnvelope(envelopeBuilder.build())
        HandlerInput handlerInput = builder.build()

        then:"A handler exists that can handle it"
        requestHandlers.find() { it.canHandle(handlerInput) }

        when:"Greet is called"

        final Optional<Response> response = handler.greet(handlerInput)

        then:"The response is present"
        response.isPresent()

        and:"The output speech is correct"
        final OutputSpeech outputSpeech = response.get().getOutputSpeech()
        outputSpeech.toString().contains(
                "Hello world"
        )
    }
}
