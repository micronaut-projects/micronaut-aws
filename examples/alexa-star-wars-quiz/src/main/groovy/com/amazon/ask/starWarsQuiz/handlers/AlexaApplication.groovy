package com.amazon.ask.starWarsQuiz.handlers

// tag::imports[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.starWarsQuiz.Question
import com.amazon.ask.starWarsQuiz.services.AnswerService
import com.amazon.ask.starWarsQuiz.services.DisplayService
import com.amazon.ask.starWarsQuiz.services.QuestionService
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazon.ask.model.Slot
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.function.aws.alexa.AlexaIntents
import io.micronaut.function.aws.alexa.annotation.IntentHandler
import javax.inject.Singleton
// end::imports[]

// tag::class[]
/**
 * Main entry point class for the Alexa Skill.
 *
 * @author Ryan Vanderwerf
 * @author Graeme Rocher
 */
@Singleton // <1>
@CompileStatic
@Slf4j // <3>
class AlexaApplication {

    private final DisplayService displayService

    private final QuestionService questionService

    private final AnswerService answerService

    AlexaApplication(DisplayService displayService,
                     QuestionService questionService,
                     AnswerService answerService) { // <4>
        this.displayService = displayService
        this.questionService = questionService
        this.answerService = answerService
    }


    /**
     * If user says HELP this is called
     * @param input
     * @return
     */
    @IntentHandler(AlexaIntents.HELP) // <2>
    Optional<Response> help(HandlerInput input) {
        String speechText = "You can say hello to me!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    /**
     * Fallback, if Alexa can't understand what the user said this will be called
     * @param input
     * @return
     */
    @IntentHandler(AlexaIntents.FALLBACK) // <7>
    Optional<Response> fallback(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Star Wars Quiz", speechText)
                .withReprompt(speechText)
                .build()
    }

    /**
     * Stops the skill/action
     * @param input
     * @return
     */
    @IntentHandler([AlexaIntents.CANCEL, AlexaIntents.STOP]) // <8>
    Optional<Response> cancel(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye")
                .withSimpleCard("Star Wars Quiz", "Goodbye")
                .build()
    }


    /**
     * This processes an answer to a question (1 to 4)
     * @param input
     * @return
     */
    @IntentHandler("AnswerIntent") // <9>
    public Optional<Response> answerIntent(HandlerInput input) {
        log.debug("inside answer intent")
        Intent intent = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent()
        Session session = input.getRequestEnvelope().getSession()
        Slot query = intent.getSlots().get("Answer")
        log.debug("raw answer ${query.name}:${query.value}")
        try {
            int guessedAnswer = Integer.parseInt(query.getValue())
            log.info("Guessed answer is:  " + query.getValue())

            return answerService.processAnswer(input,session, guessedAnswer, displayService.isSupportDisplay(session))
        } catch (NumberFormatException n) {
            return questionService.repeatQuestion(input, session, displayService.isSupportDisplay(session), true)
        }
    }

    @IntentHandler("RepeatIntent") // <10>
    public Optional<Response> repeatIntent(HandlerInput input) {
        Intent intent = ((IntentRequest) input.getRequestEnvelope().getRequest()).getIntent()
        Session session = input.getRequestEnvelope().getSession()
        Question question = (Question) session.attributes.get("lastQuestionAsked")
        String speechText = ""
        speechText += question.getSpeechText()
        askResponse(input,speechText, speechText, isSupportDisplay(session))
    }


    /**
     * Checks session if display is supported
     * @param session
     * @return
     */
    boolean isSupportDisplay(Session session) { // <5>
        boolean supportDisplay = (Boolean) session.attributes.get("supportDisplay")
        supportDisplay
    }


    /**
     * Helper to create a Spoken response
     * @param input handler input from incoming request
     * @param cardText Card text to show
     * @param speechText Speech text to say
     * @param supportDisplay is visual display supported?
     * @return
     */
    Optional<Response> askResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) { // <6>

        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addRenderTemplateDirective(displayService.buildBodyTemplate1(cardText))
                    .withSimpleCard(speechText, speechText)
                    .build()
        } else {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .withSimpleCard(speechText, speechText)
                    .build()
        }

    }

}
// end::class[]