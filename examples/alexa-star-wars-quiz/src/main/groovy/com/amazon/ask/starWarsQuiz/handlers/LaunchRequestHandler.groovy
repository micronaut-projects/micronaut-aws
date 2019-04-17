package com.amazon.ask.starWarsQuiz.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.starWarsQuiz.Question
import com.amazon.ask.starWarsQuiz.services.DisplayService
import com.amazon.ask.starWarsQuiz.services.QuestionService
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazon.ask.model.SupportedInterfaces
import com.amazon.ask.request.Predicates
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Value
import javax.inject.Inject
import javax.inject.Singleton

/**
 * You can make a intent handler it's own class like this or add it as a method annotated
 * with @IntentHandler in the main application.
 *
 * @author Ryan Vanderwerf
 */
@Singleton // <1>
@CompileStatic
class LaunchRequestHandler implements RequestHandler {  // <2>

    @Value('${quiz.number-of-questions:5}')  // <3>
    int numberOfQuestions

    @Inject DisplayService displayService
    @Inject QuestionService questionService

    @Override // <4>
    boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.requestType(LaunchRequest.class))
    }

    @Override // <5>
    Optional<Response> handle(HandlerInput input) {

        Session session = input.getRequestEnvelope().getSession()

        SupportedInterfaces supportedInterfaces = input.getRequestEnvelope().getContext().getSystem().getDevice().supportedInterfaces
        if (supportedInterfaces) {
            if (supportedInterfaces.display) {
                input.attributesManager.sessionAttributes.put("supportDisplay",true)
            }
        }

        String boldStart = (displayService.isSupportDisplay(session)) ? "<b>" : ""
        String boldEnd = (displayService.isSupportDisplay(session)) ? "</b>" : ""
        String newLine = (displayService.isSupportDisplay(session)) ? "<br/>" : "\n"
        String speechText = "Welcome to the Unofficial Star Wars Quiz.  I'm going to ask you " + numberOfQuestions + " questions to test your Star Wars knowledge.  Say repeat question at any time if you need to hear a question again, or say help if you need some help.  To answer a question, just say the number of the answer.  Let's get started:   \n\n"
        String cardText = "Welcome to the Unofficial Star Wars Quiz.  I'm going to ask you " + numberOfQuestions + " questions to test your Star Wars knowledge.  Say " + boldStart + "repeat question" + boldEnd + " at any time if you need to hear a question again, or say " + boldStart + "help" + boldEnd + " if you need some help.  To answer a question, just say the number of the answer.  Let's get started:   " + newLine + newLine

        Question question = questionService.getRandomQuestion(session)
        input.attributesManager.sessionAttributes.put("lastQuestionAsked", question)
        LinkedHashMap<String,Question> askedQuestions = new LinkedHashMap()
        askedQuestions.put(question.question,question)
        input.attributesManager.sessionAttributes.put("askedQuestions", question)

        speechText = speechText + question.getSpeechText()
        cardText = cardText + question.getCardText(displayService.isSupportDisplay(session))

        //return askResponse(input,cardText,speechText,displayService.isSupportDisplay(session))
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Unofficial Star Wars Quiz", cardText)
                .addRenderTemplateDirective(displayService.buildBodyTemplate1(cardText))
                .withReprompt(speechText)
                .build()
    }


    Optional<Response> askResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) {

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