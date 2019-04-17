package com.amazon.ask.starWarsQuiz.services

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
import groovy.util.logging.Slf4j

import javax.annotation.PostConstruct
import javax.inject.Singleton

/**
 * Saves metrics for app and user usage in DynamoDB
 *
 * @author Lee Fox
 * @author Ryan Vanderwerf
 */
@Singleton // <1>
@Slf4j
class MetricsService {

    DynamoDB dynamoDB

    @PostConstruct init() {  // <2>
        dynamoDB = new DynamoDB(new AmazonDynamoDBClient())
    }

    void questionMetricsCorrect(int questionIndex) {
        questionMetrics(questionIndex, true)
    }

    void questionMetricsWrong(int questionIndex) {
        questionMetrics(questionIndex, false)
    }

    /**
     * keeps a log of which questions often get which answers for further analysis.
     * @param questionIndex
     * @param correct
     */
    void questionMetrics(int questionIndex, boolean correct) {  // <3>

        Table table = dynamoDB.getTable("StarWarzQuizMetrics")
        log.debug("getting question id from table ${questionIndex}")
        Item item = table.getItem("id", questionIndex)
        int askedCount = 0
        int correctCount = 0
        if (item != null) {
            askedCount = item.getInt("asked")
            correctCount = item.getInt("correct")
        }
        askedCount++
        if (correct) {
            correctCount++
        }
        Item newItem = new Item()
        newItem.withInt("id", questionIndex)
        newItem.withInt("asked", askedCount)
        newItem.withInt("correct", correctCount)
        table.putItem(newItem)
    }

    /**
     * stores metrics on a per user basis
     * @param userId
     * @param score
     */
    void userMetrics(String userId, int score) { // <4>

        Table table = dynamoDB.getTable("StarWarsQuizUserMetrics")
        Item item = table.getItem("id", userId)
        int timesPlayed = 0
        int correctCount = 0
        if (item != null) {
            timesPlayed = item.getInt("timesPlayed")
            correctCount = item.getInt("lifeTimeCorrect")
        }
        timesPlayed++
        correctCount += score
        Item newItem = new Item()
        newItem.withString("id", userId)
        newItem.withInt("timesPlayed", timesPlayed)
        newItem.withInt("lifeTimeCorrect", correctCount)
        table.putItem(newItem)
    }
}
