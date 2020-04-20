package io.micronaut.aws.alexa.httpserver.controllers.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response

import static com.amazon.ask.request.Predicates.intentName

class HelpIntentHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        String speechText = "You can say hello to me!";
        input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }
}