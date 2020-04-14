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
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.order.OrderUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Creates {@link AlexaSkill} by adding request and exception handlers (({@link RequestHandler}, {@link ExceptionHandler} beans) and interceptor beans ({@link RequestInterceptor} and {@link ResponseInterceptor}).
 * @author sdelamo
 * @since 2.0.0
 */
@Singleton
public class DefaultAlexaSkillBuilder implements AlexaSkillBuilder<RequestEnvelope, ResponseEnvelope> {

    private ApplicationContext applicationContext;

    /**
     *
     * @param applicationContext Application Context
     */
    DefaultAlexaSkillBuilder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Nonnull
    @Override
     public AlexaSkill<RequestEnvelope, ResponseEnvelope> buildSkill(@Nonnull @NotNull SkillBuilder<?> skillBuilder,
                                                                     @Nullable AlexaSkillConfiguration alexaSkillConfiguration) {
        applicationContext.getBeansOfType(RequestHandler.class)
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestHandler);
        applicationContext.getBeansOfType(ExceptionHandler.class)
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addExceptionHandler);
        applicationContext.getBeansOfType(RequestInterceptor.class)
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestInterceptor);
        applicationContext.getBeansOfType(ResponseInterceptor.class)
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addResponseInterceptor);
        return alexaSkillConfiguration == null ? skillBuilder.build() :
                skillBuilder.withSkillId(alexaSkillConfiguration.getSkillId()).build();
    }
}
