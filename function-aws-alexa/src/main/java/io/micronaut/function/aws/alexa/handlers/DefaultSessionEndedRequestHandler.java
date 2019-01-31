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

package io.micronaut.function.aws.alexa.handlers;


import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.alexa.AlexaFunction;

import javax.inject.Singleton;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.requestType;

/**
 * Default session end handler that shuts down the context.
 *
 * @author graemerocher
 * @since 1.1
 */
@Singleton
public class DefaultSessionEndedRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(SessionEndedRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        // any cleanup logic goes here
        final ApplicationContext context = AlexaFunction.getCurrentAlexaApplicationContext();
        if (context != null) {
            context.close();
        }
        return input.getResponseBuilder().build();
    }
}
