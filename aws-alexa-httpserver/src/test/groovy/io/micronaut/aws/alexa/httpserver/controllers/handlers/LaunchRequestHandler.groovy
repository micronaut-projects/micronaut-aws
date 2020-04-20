package io.micronaut.aws.alexa.httpserver.controllers.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Response
import com.amazon.ask.request.Predicates

class LaunchRequestHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(LaunchRequest))
    }

    @Override
    Optional<Response> handle(HandlerInput handlerInput) {
        String speechText = "Welcome to the Alexa Skills Kit, you can say hello";
        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }
}
