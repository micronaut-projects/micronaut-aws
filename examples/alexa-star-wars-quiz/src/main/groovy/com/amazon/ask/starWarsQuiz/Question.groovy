package com.amazon.ask.starWarsQuiz

/**
 * Question pojo to represent DynamoDB row
 *
 * @author Lee Fox
 * @author Ryan Vanderwerf
 */
class Question implements Serializable {
    private static final long serialVersionUID = 1L // try never to change

    String question
    String[] options
    String speechText
    String cardText
    int answer
    int index

    String getSpeechText() {
        speechText = ""
        speechText += speechText + question + "\n\n"
        int counter = 1
        for(String option: options) {
            speechText += counter++ + "\n\n\n" + option + "\n\n\n\n"
        }
        speechText
    }

    String getCardText(boolean supportDisplay) {
        String newLine = (supportDisplay) ? "<br/>" : "\n"
        cardText = ""
        cardText += cardText + question + newLine
        int counter = 1
        for(String option: options) {
            String questionBreak = (supportDisplay) ? "\n\n" : "  "
            cardText += counter++ + questionBreak + option + newLine
        }
        cardText
    }
}
