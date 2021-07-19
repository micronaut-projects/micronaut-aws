/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
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

    private final Map<String, SkillBeans> skillBeans = new HashMap<>();
    private final SkillBeans unqualifiedSkillBeans;

    /**
     *
     * @param alexaSkillConfigurations Alexa Skill Configurations
     * @param applicationContext ApplicationContext
     */
    public DefaultAlexaSkillBuilder(Collection<AlexaSkillConfiguration> alexaSkillConfigurations,
                                    ApplicationContext applicationContext) {

        Map<String, Collection<RequestHandler>> requestHandlersBySkillName = new HashMap<>();
        Map<String, Collection<ExceptionHandler>> exceptionHandlersBySkillName = new HashMap<>();
        Map<String, Collection<RequestInterceptor>> requestInterceptorsBySkillName = new HashMap<>();
        Map<String, Collection<ResponseInterceptor>> responseInterceptorsBySkillName = new HashMap<>();

        List<String> names = alexaSkillConfigurations.stream().map(AlexaSkillConfiguration::getName).collect(Collectors.toList());
        for (String name : names) {
            requestHandlersBySkillName.put(name, applicationContext.getBeansOfType(RequestHandler.class, Qualifiers.byName(name)));
            exceptionHandlersBySkillName.put(name, applicationContext.getBeansOfType(ExceptionHandler.class, Qualifiers.byName(name)));
            requestInterceptorsBySkillName.put(name, applicationContext.getBeansOfType(RequestInterceptor.class, Qualifiers.byName(name)));
            responseInterceptorsBySkillName.put(name, applicationContext.getBeansOfType(ResponseInterceptor.class, Qualifiers.byName(name)));
        }

        Collection<RequestHandler> requestHandlers = applicationContext.getBeansOfType(RequestHandler.class);
        List<RequestHandler> unqualifiedRequestHandlers = new ArrayList<>(requestHandlers);
        for (String name : requestHandlersBySkillName.keySet()) {
            unqualifiedRequestHandlers.removeAll(requestHandlersBySkillName.get(name));
        }

        Collection<ExceptionHandler> exceptionHandlers = applicationContext.getBeansOfType(ExceptionHandler.class);
        List<ExceptionHandler> unqualifiedExceptionHandlers = new ArrayList<>(exceptionHandlers);
        for (String name : exceptionHandlersBySkillName.keySet()) {
            unqualifiedExceptionHandlers.removeAll(exceptionHandlersBySkillName.get(name));
        }

        Collection<RequestInterceptor> requestInterceptors = applicationContext.getBeansOfType(RequestInterceptor.class);
        List<RequestInterceptor> unqualifiedRequestInterceptors = new ArrayList<>(requestInterceptors);
        for (String name : requestInterceptorsBySkillName.keySet()) {
            unqualifiedRequestInterceptors.removeAll(requestInterceptorsBySkillName.get(name));
        }

        Collection<ResponseInterceptor> responseInterceptors = applicationContext.getBeansOfType(ResponseInterceptor.class);
        List<ResponseInterceptor> unqualifiedResponseInterceptors = new ArrayList<>(responseInterceptors);
        for (String name : responseInterceptorsBySkillName.keySet()) {
            unqualifiedResponseInterceptors.removeAll(responseInterceptorsBySkillName.get(name));
        }

        for (String name : names) {
            skillBeans.put(name,
                    skillBeansByName(name,
                            requestHandlersBySkillName,
                            exceptionHandlersBySkillName,
                            requestInterceptorsBySkillName,
                            responseInterceptorsBySkillName,
                            unqualifiedRequestHandlers,
                            unqualifiedExceptionHandlers,
                            unqualifiedRequestInterceptors,
                            unqualifiedResponseInterceptors));
        }
        this.unqualifiedSkillBeans = new SkillBeans(unqualifiedRequestHandlers,
                unqualifiedExceptionHandlers,
                unqualifiedRequestInterceptors,
                unqualifiedResponseInterceptors);
    }

    @NonNull
    @Override
     public AlexaSkill<RequestEnvelope, ResponseEnvelope> buildSkill(@NonNull @NotNull SkillBuilder<?> skillBuilder,
                                                                     @Nullable AlexaSkillConfiguration alexaSkillConfiguration) {

        SkillBeans skillBeans = alexaSkillConfiguration == null ? unqualifiedSkillBeans : this.skillBeans.get(alexaSkillConfiguration.getName());
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

    private SkillBeans skillBeansByName(String name,
                                        Map<String, Collection<RequestHandler>> requestHandlersBySkillName,
                                        Map<String, Collection<ExceptionHandler>> exceptionHandlersBySkillName,
                                        Map<String, Collection<RequestInterceptor>> requestInterceptorsBySkillName,
                                        Map<String, Collection<ResponseInterceptor>> responseInterceptorsBySkillName,
                                        List<RequestHandler> unqualifiedRequestHandlers,
                                        List<ExceptionHandler> unqualifiedExceptionHandlers,
                                        List<RequestInterceptor> unqualifiedRequestInterceptors,
                                        List<ResponseInterceptor> unqualifiedResponseInterceptors) {
        List<RequestHandler> requestHandlers = new ArrayList<>(unqualifiedRequestHandlers);
        if (name != null && requestHandlersBySkillName.containsKey(name)) {
            requestHandlers.addAll(requestHandlersBySkillName.get(name));
        }

        List<ExceptionHandler> exceptionHandlers = new ArrayList<>(unqualifiedExceptionHandlers);
        if (name != null && exceptionHandlersBySkillName.containsKey(name)) {
            exceptionHandlers.addAll(exceptionHandlersBySkillName.get(name));
        }

        List<RequestInterceptor> requestInterceptors = new ArrayList<>(unqualifiedRequestInterceptors);
        if (name != null && requestInterceptorsBySkillName.containsKey(name)) {
            requestInterceptors.addAll(requestInterceptorsBySkillName.get(name));
        }

        List<ResponseInterceptor> responseInterceptors = new ArrayList<>(unqualifiedResponseInterceptors);
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
