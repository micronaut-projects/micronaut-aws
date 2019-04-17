package com.amazon.ask.starWarsQuiz.services

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.starWarsQuiz.Question
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to handle the getting of quiz questions.
 *
 * @author Ryan Vanderwerf
 * @author Lee Fox
 */
@Singleton // <1>
@Slf4j
class QuestionService {

    DynamoDB dynamoDB
    int tableRowCount

    @Inject DisplayService displayService
    @Value('${quiz.number-of-questions:5}') // <2>
    int numberOfQuestions

    /**
     * This is called when the function initializes to set up the DynamoDB connection.
     */
    @PostConstruct  // <3>
    void init(){
        dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient())
        AmazonDynamoDBClient amazonDynamoDBClient
        amazonDynamoDBClient = new AmazonDynamoDBClient()
        ScanRequest req = new ScanRequest()
        req.setTableName("StarWarsQuiz")
        ScanResult result = amazonDynamoDBClient.scan(req)
        List quizItems = result.items
        tableRowCount = quizItems.size()
        log.info("This many rows in the table:  " + tableRowCount)
    }


    /**
     * Helper to get the question by its index
     *
     * @param questionIndex
     * @return
     */
    Question getQuestion(int questionIndex) { // <4>

        Table table = dynamoDB.getTable("StarWarsQuiz")
        Item item = table.getItem("Id", questionIndex)
        def questionText = item.getString("Question")
        def questionAnswer = item.getInt("answer")
        def options = new String[4]
        options[0] = item.getString("option1")
        options[1] = item.getString("option2")
        options[2] = item.getString("option3")
        options[3] = item.getString("option4")
        Question question = new Question()
        question.setQuestion(questionText)
        question.setOptions(options)
        question.setAnswer(questionAnswer)
        question.setIndex(questionIndex)
        log.info("question retrieved index:  " + question.getIndex())
        log.info("question retrieved text:SpeechletResponse  " + question.getQuestion())
        log.info("question retrieved correct:  " + question.getAnswer())
        log.info("question retrieved number of options:  " + question.getOptions().length)
        question
    }

    /**
     * Gets a random question for the quiz
     * @param session
     * @return
     */
    Question getRandomQuestion(Session session) { // <5>
        int questionIndex = (new Random().nextInt() % tableRowCount).abs()
        log.info("The question index is:  " + questionIndex)
        Question question = getQuestion(questionIndex)
        question
    }


    /**
     * lowers the question counter in the session each time a question is answered
     * @param session
     * @return
     */
    int decrementQuestionCounter(Session session) { // <6>
        log.debug("session attributes=${session.attributes}")

        int questionCounter = numberOfQuestions
        if (session.attributes?.containsKey("questionCounter")) {
            questionCounter = (int) session.attributes.get("questionCounter")
        }

        questionCounter--
        session.attributes.put("questionCounter", questionCounter)
        questionCounter

    }

    /**
     * makes sure you get a random question that has not been asked yet
     * @param session
     * @return
     */
    Question getRandomUnaskedQuestion(Session session) { // <7>
        LinkedHashMap<String, Question> askedQuestions = (LinkedHashMap) session.attributes.get("askedQuestions")
        Question question = getRandomQuestion(session)
        while(askedQuestions.get(question.getQuestion()) != null) {
            question = getRandomQuestion(session)
        }
        askedQuestions.put(question.getQuestion(), question)
        session.attributes.put("askedQuestions", askedQuestions)
        question
    }

    /**
     * Request the last asked question
     * @param input
     * @param session
     * @param supportDisplay
     * @param invalidAnswer
     * @return
     */
    Optional<Response> repeatQuestion(HandlerInput input, final Session session, boolean supportDisplay, boolean invalidAnswer) { // <8>
        Question question = (Question) session.getAttribute("lastQuestionAsked")
        String speechText = ""
        if(invalidAnswer) {
            speechText = "I didn't understand that.  Let's try again.\n\n"
        }
        speechText += question.getSpeechText()
        displayService.askResponse(input, speechText, speechText, supportDisplay)

    }
}
