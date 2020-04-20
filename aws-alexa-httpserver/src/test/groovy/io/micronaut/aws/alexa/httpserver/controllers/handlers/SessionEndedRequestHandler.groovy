package io.micronaut.aws.alexa.httpserver.controllers.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import com.amazon.ask.model.SessionEndedRequest

import static com.amazon.ask.request.Predicates.requestType

class SessionEndedRequestHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        input.matches(requestType(SessionEndedRequest));
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        // any cleanup logic goes here
        input.getResponseBuilder().build();
    }
}
