package io.micronaut.aws.alexa.httpserver.controllers.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import com.amazon.ask.request.Predicates

class HelloWorldIntentHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        input.matches(Predicates.intentName("HelloWorldIntent"));
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        String speechText = "Hello world";
        input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }
}
