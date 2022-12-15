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
package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.*;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.function.aws.event.AfterExecutionEvent;
import io.micronaut.function.executor.AbstractFunctionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * <p>An Amazon Lambda {@link RequestHandler} implementation for Micronaut {@link io.micronaut.function.FunctionBean}</p>.
 *
 * @param <I>      The request body type
 * @param <O>      The response body type
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class MicronautRequestHandler<I, O> extends AbstractFunctionExecutor<I, O, Context> implements RequestHandler<I, O>, MicronautLambdaContext {

    public static final String ENV_X_AMZN_TRACE_ID = "_X_AMZN_TRACE_ID";

    // See: https://github.com/aws/aws-xray-sdk-java/issues/251
    public static final String LAMBDA_TRACE_HEADER_PROP = "com.amazonaws.xray.traceHeader";

    /**
     * Logger for the application context creation errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MicronautRequestHandler.class);

    @SuppressWarnings("unchecked")
    private final Class<I> inputType = initTypeArgument();

    private ApplicationEventPublisher<AfterExecutionEvent> eventPublisher;

    /**
     * Default constructor; will initialize a suitable {@link ApplicationContext} for
     * Lambda deployment.
     */
    public MicronautRequestHandler() {
        try {
            buildApplicationContext(null);
            injectIntoApplicationContext();
        } catch (Exception e) {
            LOG.error("Exception initializing handler", e);
            throw e;
        }
    }

    /**
     * Constructor used to inject a preexisting {@link ApplicationContext}.
     * @param applicationContext the application context
     */
    public MicronautRequestHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        try {
            startEnvironment(applicationContext);
            injectIntoApplicationContext();
        } catch (Exception e) {
            LOG.error("Exception initializing handler: " + e.getMessage() , e);
            throw e;
        }
    }

    /**
     * Constructor used to inject a preexisting {@link ApplicationContextBuilder}.
     * @param applicationContextBuilder the application context builder
     */
    public MicronautRequestHandler(ApplicationContextBuilder applicationContextBuilder) {
        this(applicationContextBuilder.build());
    }

    private void injectIntoApplicationContext() {
        applicationContext.inject(this);
    }

    @Override
    public final O handleRequest(I input, Context context) {
        HandlerUtils.configureWithContext(this, context);
        if (!inputType.isInstance(input)) {
            input = convertInput(input);
        }
        try {
            O output = this.execute(input);
            resolveAfterExecutionPublisher().publishEvent(AfterExecutionEvent.success(context, output));
            return output;
        } catch (Throwable re) {
            resolveAfterExecutionPublisher().publishEvent(AfterExecutionEvent.failure(context, re));
            throw re;
        }
    }

    /**
     * Converts the input the required type. Subclasses can override to provide custom conversion.
     *
     * @param input The input
     * @return The converted input
     * @throws IllegalArgumentException If input cannot be converted
     */
    protected I convertInput(Object input)  {
        final ArgumentConversionContext<I> cc = ConversionContext.of(inputType);
        final Optional<I> converted = applicationContext.getConversionService().convert(
            input,
            cc
        );
        return converted.orElseThrow(() ->
            new IllegalArgumentException("Unconvertible input: " + input, cc.getLastError().map(ConversionError::getCause).orElse(null))
        );
    }

    @Override
    protected ApplicationContext buildApplicationContext(Context context) {
        applicationContext = super.buildApplicationContext(context);
        startEnvironment(applicationContext);
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return new LambdaApplicationContextBuilder();
    }

    private Class initTypeArgument() {
        final Class[] args = GenericTypeUtils.resolveSuperTypeGenericArguments(
            getClass(),
            MicronautRequestHandler.class
        );
        if (ArrayUtils.isNotEmpty(args)) {
            return args[0];
        } else {
            return Object.class;
        }
    }

    private ApplicationEventPublisher<AfterExecutionEvent> resolveAfterExecutionPublisher() {
        if (eventPublisher == null) {
            eventPublisher = applicationContext.getEventPublisher(AfterExecutionEvent.class);
        }
        return eventPublisher;
    }
}
