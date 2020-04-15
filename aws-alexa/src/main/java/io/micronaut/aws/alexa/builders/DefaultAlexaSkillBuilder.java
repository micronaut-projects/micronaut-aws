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
import io.micronaut.inject.qualifiers.Qualifiers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.lang.model.element.AnnotationValue;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates {@link AlexaSkill} by adding request and exception handlers (({@link RequestHandler}, {@link ExceptionHandler} beans) and interceptor beans ({@link RequestInterceptor} and {@link ResponseInterceptor}).
 * @author sdelamo
 * @since 2.0.0
 */
@Singleton
public class DefaultAlexaSkillBuilder implements AlexaSkillBuilder<RequestEnvelope, ResponseEnvelope> {

    private final Map<String, Collection<RequestHandler>> requestHandlersBySkillName = new HashMap<>();
    private final Map<String, Collection<ExceptionHandler>> exceptionHandlersBySkillName = new HashMap<>();
    private final Map<String, Collection<RequestInterceptor>> requestInterceptorsBySkillName = new HashMap<>();
    private final Map<String, Collection<ResponseInterceptor>> responseInterceptorsBySkillName = new HashMap<>();

    private final List<RequestHandler> unqualifiedRequestHandlers;
    private final List<ExceptionHandler> unqualifiedExceptionHandlers;
    private final List<RequestInterceptor> unqualifiedRequestInterceptors;
    private final List<ResponseInterceptor> unqualifiedResponseInterceptors;

    /**
     *
     * @param alexaSkillConfigurations Alexa Skill Configurations
     * @param applicationContext ApplicationContext
     */
    public DefaultAlexaSkillBuilder(Collection<AlexaSkillConfiguration> alexaSkillConfigurations,
                                    ApplicationContext applicationContext) {

        List<String> names = alexaSkillConfigurations.stream().map(AlexaSkillConfiguration::getName).collect(Collectors.toList());
        for (String name : names) {
            requestHandlersBySkillName.put(name, applicationContext.getBeansOfType(RequestHandler.class, Qualifiers.byName(name)));
            exceptionHandlersBySkillName.put(name, applicationContext.getBeansOfType(ExceptionHandler.class, Qualifiers.byName(name)));
            requestInterceptorsBySkillName.put(name, applicationContext.getBeansOfType(RequestInterceptor.class, Qualifiers.byName(name)));
            responseInterceptorsBySkillName.put(name, applicationContext.getBeansOfType(ResponseInterceptor.class, Qualifiers.byName(name)));
        }
        Collection<RequestHandler> requestHandlers = applicationContext.getBeansOfType(RequestHandler.class);
        List<RequestHandler> requestHandlersList = new ArrayList<>(requestHandlers);
        for (String name : requestHandlersBySkillName.keySet()) {
            requestHandlersList.removeAll(requestHandlersBySkillName.get(name));
        }
        this.unqualifiedRequestHandlers = requestHandlersList;

        Collection<ExceptionHandler> exceptionHandlers = applicationContext.getBeansOfType(ExceptionHandler.class);
        List<ExceptionHandler> exceptionHandlersList = new ArrayList<>(exceptionHandlers);
        for (String name : exceptionHandlersBySkillName.keySet()) {
            exceptionHandlersList.removeAll(exceptionHandlersBySkillName.get(name));
        }
        this.unqualifiedExceptionHandlers = exceptionHandlersList;

        Collection<RequestInterceptor> requestInterceptors = applicationContext.getBeansOfType(RequestInterceptor.class);

        List<RequestInterceptor> requestInterceptorsList = new ArrayList<>(requestInterceptors);
        for (String name : requestInterceptorsBySkillName.keySet()) {
            requestInterceptorsList.removeAll(requestInterceptorsBySkillName.get(name));
        }
        this.unqualifiedRequestInterceptors = requestInterceptorsList;

        Collection<ResponseInterceptor> responseInterceptors = applicationContext.getBeansOfType(ResponseInterceptor.class);
        List<ResponseInterceptor> responseInterceptorsList = new ArrayList<>(responseInterceptors);
        for (String name : responseInterceptorsBySkillName.keySet()) {
            responseInterceptorsList.removeAll(responseInterceptorsBySkillName.get(name));
        }
        this.unqualifiedResponseInterceptors = responseInterceptorsList;
    }

    @Nonnull
    @Override
     public AlexaSkill<RequestEnvelope, ResponseEnvelope> buildSkill(@Nonnull @NotNull SkillBuilder<?> skillBuilder,
                                                                     @Nullable AlexaSkillConfiguration alexaSkillConfiguration) {

        SkillBeans skillBeans = skillBeansByName(alexaSkillConfiguration != null ? alexaSkillConfiguration.getName() : null);
        skillBeans.getRequestHandlers()
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestHandler);

        skillBeans.getExceptionHandlers()
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addExceptionHandler);

        skillBeans.getRequestInterceptors()
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addRequestInterceptor);

        skillBeans.getResponseInterceptors()
                .stream()
                .sorted(OrderUtil.COMPARATOR)
                .forEach(skillBuilder::addResponseInterceptor);

        if (alexaSkillConfiguration != null) {
            skillBuilder = skillBuilder.withSkillId(alexaSkillConfiguration.getSkillId());
        }
        return skillBuilder.build();
    }

    private SkillBeans skillBeansByName(String name) {
        List<RequestHandler> requestHandlers = new ArrayList<>();
        requestHandlers.addAll(unqualifiedRequestHandlers);
        if (name != null && requestHandlersBySkillName.containsKey(name)) {
            requestHandlers.addAll(requestHandlersBySkillName.get(name));
        }

        List<ExceptionHandler> exceptionHandlers = new ArrayList<>();
        exceptionHandlers.addAll(unqualifiedExceptionHandlers);
        if (name != null && exceptionHandlersBySkillName.containsKey(name)) {
            exceptionHandlers.addAll(exceptionHandlersBySkillName.get(name));
        }

        List<RequestInterceptor> requestInterceptors = new ArrayList<>();
        requestInterceptors.addAll(unqualifiedRequestInterceptors);
        if (name != null && requestInterceptorsBySkillName.containsKey(name)) {
            requestInterceptors.addAll(requestInterceptorsBySkillName.get(name));
        }

        List<ResponseInterceptor> responseInterceptors = new ArrayList<>();
        responseInterceptors.addAll(unqualifiedResponseInterceptors);
        if (name != null && responseInterceptorsBySkillName.containsKey(name)) {
            responseInterceptors.addAll(responseInterceptorsBySkillName.get(name));
        }

        return new SkillBeans(requestHandlers,
                exceptionHandlers,
                requestInterceptors,
                responseInterceptors);
    }

    /**
     * Beans for a particular Alexa Skills.
     */
    private static class SkillBeans {

        private List<RequestHandler> requestHandlers;
        private List<ExceptionHandler> exceptionHandlers;
        private List<RequestInterceptor> requestInterceptors;
        private List<ResponseInterceptor> responseInterceptors;

        public SkillBeans(List<RequestHandler> requestHandlers,
                          List<ExceptionHandler> exceptionHandlers,
                          List<RequestInterceptor> requestInterceptors,
                          List<ResponseInterceptor> responseInterceptors) {
            this.requestHandlers = requestHandlers;
            this.exceptionHandlers = exceptionHandlers;
            this.requestInterceptors = requestInterceptors;
            this.responseInterceptors = responseInterceptors;
        }

        public List<RequestHandler> getRequestHandlers() {
            return requestHandlers;
        }

        public void setRequestHandlers(List<RequestHandler> requestHandlers) {
            this.requestHandlers = requestHandlers;
        }

        public List<ExceptionHandler> getExceptionHandlers() {
            return exceptionHandlers;
        }

        public void setExceptionHandlers(List<ExceptionHandler> exceptionHandlers) {
            this.exceptionHandlers = exceptionHandlers;
        }

        public List<RequestInterceptor> getRequestInterceptors() {
            return requestInterceptors;
        }

        public void setRequestInterceptors(List<RequestInterceptor> requestInterceptors) {
            this.requestInterceptors = requestInterceptors;
        }

        public List<ResponseInterceptor> getResponseInterceptors() {
            return responseInterceptors;
        }

        public void setResponseInterceptors(List<ResponseInterceptor> responseInterceptors) {
            this.responseInterceptors = responseInterceptors;
        }
    }
}
