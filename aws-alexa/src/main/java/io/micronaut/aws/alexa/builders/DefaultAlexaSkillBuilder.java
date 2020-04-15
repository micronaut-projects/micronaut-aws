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
package io.micronaut.aws.alexa.builders;

import com.amazon.ask.AlexaSkill;
import com.amazon.ask.builder.SkillBuilder;
import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.interceptor.RequestInterceptor;
import com.amazon.ask.dispatcher.request.interceptor.ResponseInterceptor;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration;
import io.micronaut.core.order.OrderUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * Creates {@link AlexaSkill} by adding request and exception handlers (({@link RequestHandler}, {@link ExceptionHandler} beans) and interceptor beans ({@link RequestInterceptor} and {@link ResponseInterceptor}).
 * @author sdelamo
 * @since 2.0.0
 */
@Singleton
public class DefaultAlexaSkillBuilder implements AlexaSkillBuilder<RequestEnvelope, ResponseEnvelope> {

    private final Collection<RequestHandler> requestHandlers;
    private final Collection<ExceptionHandler> exceptionHandlers;
    private final Collection<RequestInterceptor> requestInterceptors;
    private final Collection<ResponseInterceptor> responseInterceptors;

    /**
     *
     * @param requestHandlers Request Handlers
     * @param exceptionHandlers Exceptions Handlers
     * @param requestInterceptors Request Interceptors
     * @param responseInterceptors Response Interceptors
     */
    public DefaultAlexaSkillBuilder(Collection<RequestHandler> requestHandlers,
                                    Collection<ExceptionHandler> exceptionHandlers,
                                    Collection<RequestInterceptor> requestInterceptors,
                                    Collection<ResponseInterceptor> responseInterceptors) {
        this.requestHandlers = requestHandlers;
        this.exceptionHandlers = exceptionHandlers;
        this.requestInterceptors = requestInterceptors;
        this.responseInterceptors = responseInterceptors;
    }

    @Nonnull
    @Override
     public AlexaSkill<RequestEnvelope, ResponseEnvelope> buildSkill(@Nonnull @NotNull SkillBuilder<?> skillBuilder,
                                                                     @Nullable AlexaSkillConfiguration alexaSkillConfiguration) {
        requestHandlers
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestHandler);
        exceptionHandlers
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addExceptionHandler);
        requestInterceptors
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestInterceptor);
        responseInterceptors
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addResponseInterceptor);
        return alexaSkillConfiguration == null ? skillBuilder.build() :
                skillBuilder.withSkillId(alexaSkillConfiguration.getSkillId()).build();
    }
}
