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
package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.function.executor.AbstractFunctionExecutor;

import javax.annotation.Nonnull;
import java.util.Optional;

import static io.micronaut.function.aws.MicronautLambdaContext.ENVIRONMENT_LAMBDA;

/**
 * <p>An Amazon Lambda {@link RequestHandler} implementation for Micronaut {@link io.micronaut.function.FunctionBean}</p>.
 *
 * @param <I>      The request body type
 * @param <O>      The response body type
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class MicronautRequestHandler<I, O> extends AbstractFunctionExecutor<I, O, Context> implements RequestHandler<I, O>, MicronautLambdaContext {

    @SuppressWarnings("unchecked")
    private final Class<I> inputType = initTypeArgument();

    private final MicronautRequestHandler<I, O> injectedInstance;

    public MicronautRequestHandler() {
        buildApplicationContext(null);
        injectedInstance = applicationContext.inject(this);
    }

    @Override
    public final O handleRequest(I input, Context context) {

        if (context != null) {
            registerContextBeans(context, applicationContext);
        }

        if (!inputType.isInstance(input)) {
            input = convertInput(input);
        }
        return injectedInstance.execute(input);
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

    @Nonnull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.environments(ENVIRONMENT_LAMBDA);
        return builder;
    }

    /**
     * Register the beans in the application.
     *
     * @param context context
     * @param applicationContext application context
     */
    static void registerContextBeans(Context context, ApplicationContext applicationContext) {
        applicationContext.registerSingleton(context);
        LambdaLogger logger = context.getLogger();
        if (logger != null) {
            applicationContext.registerSingleton(logger);
        }
        ClientContext clientContext = context.getClientContext();
        if (clientContext != null) {
            applicationContext.registerSingleton(clientContext);
        }
        CognitoIdentity identity = context.getIdentity();
        if (identity != null) {
            applicationContext.registerSingleton(identity);
        }
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
