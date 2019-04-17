package com.amazon.ask.starWarsQuiz.services

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.starWarsQuiz.Question
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to process and return a response to a question (answer)
 *
 * @author Ryan Vanderwerf
 * @author Lee Fox
 */
@Singleton // <1>
@Slf4j
class AnswerService {

    @Inject // <2>
    DisplayService displayService
    @Inject
    QuestionService questionService
    @Inject
    MetricsService metricsService

    /**
     * handles the answer the user gives to the questions to determine if it's correct or now
     * @param input
     * @param session
     * @param guessedAnswer
     * @param supportDisplay
     * @return
     */
    Optional<Response> processAnswer(HandlerInput input, Session session, int guessedAnswer, boolean supportDisplay) { // <3>
        def speechText
        def cardText
        String newLine = (displayService.isSupportDisplay(session)) ? "<br/>" : "\n"

        Question question = (Question) session.attributes.get("lastQuestionAsked")
        def answer = question.getAnswer()
        log.info("correct answer is:  " + answer)
        log.info "question was:  " + question.question

        int questionCounter = questionService.decrementQuestionCounter(session)

        if (guessedAnswer == answer) {
            speechText = "You got it right.\n\n"
            cardText = "You got it right." + newLine + newLine
            int score = 0
            if (input.attributesManager.sessionAttributes.containsKey("score")) {
                score = (Integer) input.attributesManager.sessionAttributes.get("score")
            }
            ++score
            input.attributesManager.sessionAttributes.put("score", score)
            metricsService.questionMetricsCorrect(question.getIndex())
        } else {
            speechText = "You got it wrong.\n\n"
            cardText = "You got it wrong." + newLine + newLine
            metricsService.questionMetricsWrong(question.getIndex())
        }

        log.info("questionCounter:  " + questionCounter)

        if (questionCounter > 0) {
            session.attributes.put("state", "askQuestion")
            question = questionService.getRandomUnaskedQuestion(session)
            session.attributes.put("lastQuestionAsked", question)
            speechText += question.getSpeechText()
            cardText += question.getCardText(supportDisplay)
            return displayService.askResponse(input, cardText, speechText, supportDisplay)
        } else {
            int score = (Integer) input.attributesManager.sessionAttributes.get("score")
            speechText += "\n\nYou answered ${score} questions correctly.\n\nThank you for playing."
            cardText += "You answered ${score} questions correctly.  " + newLine + newLine +"Thank you for playing."
            metricsService.userMetrics(session.getUser().userId, score)
            return displayService.tellResponse(input,cardText, speechText, supportDisplay)
        }
    }
}
