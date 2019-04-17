package com.amazon.ask.starWarsQuiz.services

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import com.amazon.ask.model.Session
import com.amazon.ask.model.interfaces.display.BodyTemplate1
import com.amazon.ask.model.interfaces.display.Image
import com.amazon.ask.model.interfaces.display.ImageInstance
import com.amazon.ask.model.interfaces.display.RichText
import com.amazon.ask.model.interfaces.display.Template
import com.amazon.ask.model.interfaces.display.TextContent
import javax.inject.Singleton

/**
 * Helps generates responses for the right device type.
 *
 * @author Ryan Vanderwerf
 */
@Singleton // <1>
class DisplayService {

    /**
     * Helper to determine if a display is supported or not in the session
     * @param session
     * @return
     */
    boolean isSupportDisplay(Session session) {
        boolean supportDisplay = false

        if (session?.attributes?.containsKey("supportDisplay")) {
            supportDisplay = (Boolean) session.attributes.get("supportDisplay")
        }
        supportDisplay
    }

    /**
     * Helper to ask user a question that expects a response with given card and speech text
     * @param input
     * @param cardText
     * @param speechText
     * @param supportDisplay
     * @return
     */
    Optional<Response> askResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) { // <2>


        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(speechText)
                    .addRenderTemplateDirective(buildBodyTemplate1(cardText))
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

    /**
     * Helper to give a response to a user that does not look for a response to the user (the end)
     * @param input
     * @param cardText card text to display
     * @param speechText speech text to say
     * @param supportDisplay
     * @return
     */
    Optional<Response> tellResponse(HandlerInput input, String cardText, String speechText, boolean supportDisplay) { // <3>


        if (supportDisplay) {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .addRenderTemplateDirective(buildBodyTemplate1(cardText))
                    .withSimpleCard(speechText, speechText)
                    .build()
        } else {
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard(speechText, speechText)
                    .build()
        }

    }

    /**
     * Helper to build a bodytemplate1 for display devices
     * @param cardText
     * @return
     */
    Template buildBodyTemplate1(String cardText) { // <4>

        return BodyTemplate1.builder()
                .withBackgroundImage(getImageInstance("https://media.giphy.com/media/YJNOIvcwG1NcY/giphy.gif"))
                .withTitle("Unofficial Star Wars Quiz")
                .withTextContent(getTextContent(cardText, cardText))
                .build()

    }


    /**
     * Creates an image instance for use in a body template
     * @param imageUrl
     * @return
     */
    Image getImageInstance(String imageUrl) { // <5>
        List<ImageInstance> instances = new ArrayList<>()
        ImageInstance instance = ImageInstance.builder()
                .withUrl(imageUrl)
                .build()
        instances.add(instance)
        instances
        Image.builder()
                .withSources(instances).build()
    }

    /**
     * TextContent builder helper for a bodytemplate
     * @param primaryText
     * @param secondaryText
     * @return
     */
    TextContent getTextContent(String primaryText, String secondaryText) { // <6>
        return TextContent.builder()
                .withPrimaryText(makeRichText(primaryText))
                .withSecondaryText(makeRichText(secondaryText))
                .build()
    }

    /**
     * RichText builder helper for a bodytemplate
     * @param text
     * @return
     */
    RichText makeRichText(String text) {
        return RichText.builder()
                .withText(text)
                .build()
    }
}
