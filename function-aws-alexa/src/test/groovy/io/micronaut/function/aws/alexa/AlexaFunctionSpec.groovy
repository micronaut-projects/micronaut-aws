/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.alexa

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Response
import io.micronaut.context.ApplicationContext
import io.micronaut.context.DefaultApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.function.aws.alexa.handlers.AnnotatedRequestHandler
import io.micronaut.function.aws.alexa.handlers.DefaultSessionEndedRequestHandler
import spock.lang.Specification

import javax.inject.Singleton
import java.beans.beancontext.BeanContext

class AlexaFunctionSpec extends Specification {

    void "test init"() {
        when:
        // no good way to pass in run properties so this is a workaround to test
        System.setProperty("alexa.skill-id","23132234234234324dsf")
        AlexaFunction function = new AlexaFunction()
        def context = AlexaFunction.currentAlexaApplicationContext

        then:
        context.isRunning()
        context.getBean(AlexaConfiguration.class).skillId == "23132234234234324dsf"

        when:
        def requestHandlers = context.getBeansOfType(RequestHandler)

        then:
        requestHandlers.size() == 3
        requestHandlers.find { it instanceof DefaultSessionEndedRequestHandler}
        requestHandlers.find { it instanceof MyHandler}

        when:
        AnnotatedRequestHandler handler = requestHandlers.find { it instanceof AnnotatedRequestHandler }

        then:
        handler

        when:
        final HandlerInput.Builder builder = HandlerInput.builder();
        final RequestEnvelope.Builder envelopeBuilder = RequestEnvelope.builder();
        def intentBuilder = IntentRequest.builder()
        intentBuilder.withIntent(Intent.builder().withName("HelloWorldIntent").build())
        envelopeBuilder.withRequest(intentBuilder.build())
        builder.withRequestEnvelope(envelopeBuilder.build())


        then:
        handler.canHandle(builder.build())

        cleanup:
        function?.close()
    }


    void "test init - no config"() {
        when:
        System.clearProperty("alexa.skill-id")
        AlexaFunction function = new AlexaFunction()
        def context = AlexaFunction.currentAlexaApplicationContext

        then:
        context.isRunning()
        context.getBean(AlexaConfiguration.class).skillId == null

        when:
        def requestHandlers = context.getBeansOfType(RequestHandler)

        then:
        requestHandlers.size() == 3
        requestHandlers.find { it instanceof DefaultSessionEndedRequestHandler}
        requestHandlers.find { it instanceof MyHandler}

        when:
        AnnotatedRequestHandler handler = requestHandlers.find { it instanceof AnnotatedRequestHandler }

        then:
        handler

        when:
        final HandlerInput.Builder builder = HandlerInput.builder();
        final RequestEnvelope.Builder envelopeBuilder = RequestEnvelope.builder();
        def intentBuilder = IntentRequest.builder()
        intentBuilder.withIntent(Intent.builder().withName("HelloWorldIntent").build())
        envelopeBuilder.withRequest(intentBuilder.build())
        builder.withRequestEnvelope(envelopeBuilder.build())


        then:
        handler.canHandle(builder.build())

        cleanup:
        function?.close()
    }


    @Singleton
    static class MyHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput handlerInput) {
            return false
        }

        @Override
        Optional<Response> handle(HandlerInput handlerInput) {
            return Optional.empty()
        }
    }
}
