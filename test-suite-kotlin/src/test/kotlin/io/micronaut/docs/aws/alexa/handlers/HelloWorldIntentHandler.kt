package io.micronaut.docs.aws.alexa.handlers

//tag::clazz[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Response
import com.amazon.ask.request.Predicates
import jakarta.inject.Singleton
import java.util.Optional

@Singleton // <1>
class HelloWorldIntentHandler : RequestHandler {

    override fun canHandle(input: HandlerInput): Boolean {
        return input.matches(Predicates.intentName("HelloWorldIntent"))
    }

    override fun handle(input: HandlerInput): Optional<Response> {
        val speechText = "Hello world"
        return input.responseBuilder
            .withSpeech(speechText)
            .withSimpleCard("HelloWorld", speechText)
            .build()
    }

}
//end::clazz[]
