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
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.function.aws.event.AfterExecutionEvent;
import io.micronaut.function.executor.StreamFunctionExecutor;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>An implementation of the {@link RequestStreamHandler} for Micronaut</p>.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class MicronautRequestStreamHandler extends StreamFunctionExecutor<Context> implements RequestStreamHandler, MicronautLambdaContext {

    /**
     * Logger for the application context creation errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MicronautRequestStreamHandler.class);
    
    private ApplicationEventPublisher<AfterExecutionEvent> eventPublisher;

    @Nullable
    private String ctxFunctionName;

    /**
     * Default constructor; will initialize a suitable {@link ApplicationContext} for
     * Lambda deployment.
     */
    public MicronautRequestStreamHandler() {
        // initialize the application context in the constructor
        // this is faster in Lambda as init cost is giving higher processor priority
        // see https://github.com/micronaut-projects/micronaut-aws/issues/18#issuecomment-530903419
        try {
            buildApplicationContext(null);
        } catch (Exception e) {
            LOG.error("Exception initializing handler: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Constructor used to inject a preexisting {@link ApplicationContext}.
     * @param applicationContext the application context
     */
    public MicronautRequestStreamHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        if (context != null) {
            this.ctxFunctionName = context.getFunctionName();
        }
        HandlerUtils.configureWithContext(this, context);
        try {
            execute(input, output, context);
            resolveAfterExecutionPublisher().publishEvent(AfterExecutionEvent.success(null));
        } catch (Throwable e) {
            resolveAfterExecutionPublisher().publishEvent(AfterExecutionEvent.failure(e));
            throw e;
        }
    }

    @Override
    protected ApplicationContext buildApplicationContext(Context context) {
        return super.buildApplicationContext(context);
    }

    @NonNull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return new LambdaApplicationContextBuilder();
    }

    @Override
    protected String resolveFunctionName(Environment env) {
        String functionName = super.resolveFunctionName(env);
        return (functionName != null) ? functionName : ctxFunctionName;
    }

    private ApplicationEventPublisher<AfterExecutionEvent> resolveAfterExecutionPublisher() {
        if (eventPublisher == null) {
            eventPublisher = applicationContext.getEventPublisher(AfterExecutionEvent.class);
        }
        return eventPublisher;
    }

    @Override
    public void close() {
        // Don't close the application context
        // ApplicationContext will be closed via the `Runtime.getRuntime().addShutdownHook` added at {@link AbstractExecutor#buildApplicationContext}
    }
}
