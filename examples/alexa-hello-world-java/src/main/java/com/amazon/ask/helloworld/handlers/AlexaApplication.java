package com.amazon.ask.helloworld.handlers;

// tag::imports[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import io.micronaut.function.aws.alexa.AlexaIntents;
import io.micronaut.function.aws.alexa.annotation.IntentHandler;

import javax.inject.Singleton;
import java.util.Optional;
// end::imports[]

// tag::class[]
@Singleton // <1>
public class AlexaApplication {

    public static final String INTENT_NAME = "HelloWorldIntent";

    private final MessageService messageService;

    public AlexaApplication(MessageService messageService) { // <2>
        this.messageService = messageService;
    }

    @IntentHandler(INTENT_NAME) // <3>
    public Optional<Response> greet(HandlerInput input) { // <4>
        String speechText = messageService.sayHello();
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build();
    }
// end::class[]

    @IntentHandler(AlexaIntents.HELP)
    public Optional<Response> help(HandlerInput input) {
        String speechText = "You can say hello to me!";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }

    @IntentHandler(AlexaIntents.FALLBACK)
    public Optional<Response> fallback(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }

    @IntentHandler({AlexaIntents.CANCEL, AlexaIntents.STOP})
    public Optional<Response> cancel(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye")
                .withSimpleCard("HelloWorld", "Goodbye")
                .build();
    }
}
