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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.function.executor.AbstractFunctionExecutor;

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

    @SuppressWarnings("unchecked")
    private final Class<I> inputType = initTypeArgument();

    /**
     * Default constructor; will initialize a suitable {@link ApplicationContext} for
     * Lambda deployment.
     */
    public MicronautRequestHandler() {
        buildApplicationContext(null);
        injectIntoApplicationContext();
    }

    /**
     * Constructor used to inject a preexisting {@link ApplicationContext}.
     * @param applicationContext the application context
     */
    public MicronautRequestHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        startEnvironment(applicationContext);
        injectIntoApplicationContext();
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
        return this.execute(input);
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
}
