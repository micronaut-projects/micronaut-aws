package io.micronaut.docs.aws.alexa.handlers

//tag::clazz[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import com.amazon.ask.request.Predicates
import groovy.transform.CompileStatic
import jakarta.inject.Singleton

@CompileStatic
@Singleton // <1>
class HelloWorldIntentHandler implements RequestHandler {

    @Override
    boolean canHandle(HandlerInput input) {
        input.matches(Predicates.intentName("HelloWorldIntent"))
    }

    @Override
    Optional<Response> handle(HandlerInput input) {
        String speechText = "Hello world"
        input.responseBuilder
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }

}
//end::clazz[]
