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
package io.micronaut.function.aws.alexa;

import com.amazon.ask.AlexaSkill;
import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.amazon.ask.builder.SkillBuilder;
import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.dispatcher.request.interceptor.RequestInterceptor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * This is the base function you extend for Alexa skills support. For now you have to override apply but just call super() in it.
 * Your skill itself goes in implementing getSkill() and adding handlers for your intents.
 *
 * @author Ryan Vanderwerf
 * @author Graeme Rocher
 */
public class AlexaFunction extends SkillStreamHandler implements AutoCloseable, Closeable {

    /**
     * Environment used for setup.
     */
    public static final String ENV_ALEXA = "alexa";

    private static ApplicationContext staticApplicationContext;

    /**
     * Default constructor.
     */
    public AlexaFunction() {
        this(new AlexaSkill[0]);
    }

    /**
     * Used to construct a function with a custom context builder.
     *
     * @param contextBuilder The context builder.
     */
    public AlexaFunction(ApplicationContextBuilder contextBuilder) {
        this(contextBuilder, new AlexaSkill[0]);
    }

    /**
     * Used to construct a function with a custom context builder.
     *
     * @param skillBuilder The skill builder.
     */
    public AlexaFunction(SkillBuilder<?> skillBuilder) {
        this(skillBuilder, ApplicationContext.build(), new AlexaSkill[0]);
    }

    /**
     * Default contructor.
     *
     * @param skills The skills to include
     */
    public AlexaFunction(AlexaSkill... skills) {
        this(ApplicationContext.build(), skills);
    }

    /**
     * Used to construct a function with a custom context builder.
     *
     * @param contextBuilder The context builder.
     * @param skills The skills
     */
    public AlexaFunction(ApplicationContextBuilder contextBuilder, AlexaSkill... skills) {
        this(Skills.standard(), contextBuilder, skills);
    }

    /**
     * Used to construct a function with a custom context builder.
     *
     * @param skillBuilder The skill builder
     * @param contextBuilder The context builder.
     */
    public AlexaFunction(SkillBuilder<?> skillBuilder, ApplicationContextBuilder contextBuilder) {
        this(skillBuilder, contextBuilder, new AlexaSkill[0]);
    }

    /**
     * Used to construct a function with a custom context builder.
     *
     * @param skillBuilder The skill builder
     * @param contextBuilder The context builder.
     * @param skills The skills
     */
    protected AlexaFunction(SkillBuilder<?> skillBuilder, ApplicationContextBuilder contextBuilder, AlexaSkill... skills) {
        super(initAlexaFunction(skillBuilder, contextBuilder, skills));
        staticApplicationContext.inject(this);
    }

    /**
     * Obtain the current Alexa application context.
     *
     * @return The context
     */
    public static ApplicationContext getCurrentAlexaApplicationContext() {
        return staticApplicationContext;
    }

    private static AlexaSkill[] initAlexaFunction(
            SkillBuilder<?> skillBuilder,
            ApplicationContextBuilder contextBuilder,
            AlexaSkill... skills) {
        ArgumentUtils.requireNonNull("skillBuilder", skillBuilder);
        ArgumentUtils.requireNonNull("contextBuilder", contextBuilder);
        // Avoid extra lookups
        System.setProperty(Environment.CLOUD_PLATFORM_PROPERTY, Environment.AMAZON_EC2);
        contextBuilder.environments(Environment.FUNCTION, ENV_ALEXA);
        final ApplicationContext applicationContext = contextBuilder.build().start();
        staticApplicationContext = applicationContext;
        final AlexaSkill[] array = applicationContext.getBeansOfType(
                AlexaSkill.class
        ).toArray(new AlexaSkill[0]);

        final boolean hasSkills = ArrayUtils.isNotEmpty(skills);
        if (hasSkills) {
            for (AlexaSkill skill : skills) {
                applicationContext.inject(skill);
            }
            final AlexaSkill[] all = ArrayUtils.concat(array, skills);
            OrderUtil.sort(all);

            return all;
        } else {

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

            final Skill skill = skillBuilder.build();
            return new AlexaSkill[] { skill };
        }
    }

    @Override
    public void close() throws IOException {
        if (staticApplicationContext != null && staticApplicationContext.isRunning()) {
            staticApplicationContext.close();
        }
    }
}
