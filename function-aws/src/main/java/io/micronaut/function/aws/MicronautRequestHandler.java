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
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.function.executor.AbstractFunctionExecutor;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
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
    public static final String MDC_DEFAULT_AWS_REQUEST_ID = "AWSRequestId";
    public static final String MDC_DEFAULT_FUNCTION_NAME = "AWSFunctionName";
    public static final String MDC_DEFAULT_FUNCTION_VERSION = "AWSFunctionVersion";
    public static final String MDC_DEFAULT_FUNCTION_ARN = "AWSFunctionArn";
    public static final String MDC_DEFAULT_FUNCTION_MEMORY_SIZE = "AWSFunctionMemoryLimit";
    public static final String MDC_DEFAULT_FUNCTION_REMAINING_TIME = "AWSFunctionRemainingTime";
    public static final String MDC_DEFAULT_XRAY_TRACE_ID = "AWS-XRAY-TRACE-ID";

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
        injectIntoApplicationContext();
    }

    private void injectIntoApplicationContext() {
        applicationContext.inject(this);
    }

    @Override
    public final O handleRequest(I input, Context context) {
        if (context != null) {
            registerContextBeans(context, applicationContext);
            populateMappingDiagnosticContextValues(context);
        }
        populateMappingDiagnosticContextWithXrayTraceId();
        if (!inputType.isInstance(input)) {
            input = convertInput(input);
        }
        return this.execute(input);
    }

    /**
     * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/java-logging.html">AWS Lambda function logging in Java</a>
     * @param context The Lambda execution environment context object.
     */
    protected void populateMappingDiagnosticContextValues(@NonNull Context context) {
        if (context.getAwsRequestId() != null) {
            mdcput(MDC_DEFAULT_AWS_REQUEST_ID, context.getAwsRequestId());
        }
        if (context.getFunctionName() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_NAME, context.getFunctionName());
        }
        if (context.getFunctionVersion() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_VERSION, context.getFunctionVersion());
        }
        if (context.getInvokedFunctionArn() != null) {
            mdcput(MDC_DEFAULT_FUNCTION_ARN, context.getInvokedFunctionArn());
        }
        mdcput(MDC_DEFAULT_FUNCTION_MEMORY_SIZE, String.valueOf(context.getMemoryLimitInMB()));
        mdcput(MDC_DEFAULT_FUNCTION_REMAINING_TIME, String.valueOf(context.getRemainingTimeInMillis()));
    }

    /**
     * Put a diagnostic context value.
     * @param key non-null key
     * @param val value to put in the map
     * @throws IllegalArgumentException in case the "key" parameter is null
     */
    protected void mdcput(@NonNull String key, @NonNull String val) throws IllegalArgumentException {
        MDC.put(key, val);
    }

    /**
     * Populate MDC with XRay Trace ID if is able to parse it.
     */
    protected void populateMappingDiagnosticContextWithXrayTraceId() {
        parseXrayTraceId().ifPresent(xrayTraceId -> mdcput(MDC_DEFAULT_XRAY_TRACE_ID, xrayTraceId));
    }

    /**
     * Parses XRay Trace ID from _X_AMZN_TRACE_ID environment variable.
     * @see <a href="https://docs.aws.amazon.com/xray/latest/devguide/xray-sdk-java-configuration.html">Trace ID injection into logs</a>
     * @return Trace id or empty if not found
     */
    @NonNull
    protected static Optional<String> parseXrayTraceId() {
        final String X_AMZN_TRACE_ID = System.getenv(ENV_X_AMZN_TRACE_ID);
        if (X_AMZN_TRACE_ID != null) {
            return Optional.of(X_AMZN_TRACE_ID.split(";")[0].replace("Root=", ""));
        }
        return Optional.empty();
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
    @Nonnull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return new LambdaApplicationContextBuilder();
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
