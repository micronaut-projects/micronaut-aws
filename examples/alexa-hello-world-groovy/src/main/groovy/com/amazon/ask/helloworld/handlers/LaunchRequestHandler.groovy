package com.amazon.ask.helloworld.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Response
import com.amazon.ask.request.Predicates

import javax.inject.Singleton
import java.util.Optional

@Singleton
class LaunchRequestHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.requestType(LaunchRequest.class))
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        String speechText = "Welcome to the Alexa Skills Kit, you can say hello"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

}