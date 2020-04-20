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

import com.amazon.ask.AlexaSkill
import com.amazon.ask.CustomSkill
import com.amazon.ask.Skill
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Response
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration
import io.micronaut.aws.alexa.handlers.AnnotatedRequestHandler
import io.micronaut.context.ApplicationContext
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import javax.inject.Singleton

class AlexaFunctionSpec extends Specification {

    @RestoreSystemProperties
    void "test init"() {
        when:
        // no good way to pass in run properties so this is a workaround to test
        System.setProperty("alexa.skills.helloworld.skill-id","23132234234234324dsf")
        AlexaFunction function = new AlexaFunction()
        ApplicationContext context = function.applicationContext
        context.containsBean(AlexaSkill)
        context.containsBean(Skill)

        then:
        context.isRunning()
        context.getBean(AlexaSkillConfiguration.class).skillId == "23132234234234324dsf"

        when:
        def requestHandlers = context.getBeansOfType(RequestHandler)

        then:
        requestHandlers.size() == 2
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
        AlexaFunction function = new AlexaFunction()
        ApplicationContext context = function.applicationContext

        then:
        context.isRunning()
        !context.containsBean(AlexaSkillConfiguration.class)
        context.containsBean(Skill)
        context.containsBean(AlexaSkill)

        when:
        AlexaSkill alexaSkill = context.getBean(AlexaSkill)

        then:
        noExceptionThrown()
        alexaSkill instanceof CustomSkill
        !((CustomSkill) alexaSkill).skillId

        when:
        def requestHandlers = context.getBeansOfType(RequestHandler)

        then:
        requestHandlers.size() == 2
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
