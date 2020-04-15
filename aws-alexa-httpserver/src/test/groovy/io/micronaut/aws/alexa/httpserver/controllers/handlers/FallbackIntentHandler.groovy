package io.micronaut.aws.alexa.httpserver.controllers.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response

import static com.amazon.ask.request.Predicates.intentName

class FallbackIntentHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.FallbackIntent"))
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }
}
