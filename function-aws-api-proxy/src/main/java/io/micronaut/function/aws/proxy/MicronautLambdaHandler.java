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
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.Introspected;

import java.io.Closeable;
import java.io.IOException;

/**
 * AWS {@link RequestHandler} for {@link AwsProxyRequest} and {@link AwsProxyResponse}.
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
public class MicronautLambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse>, ApplicationContextProvider, Closeable {

    protected final MicronautLambdaContainerHandler handler;

    /**
     * Constructor.
     * @throws ContainerInitializationException thrown intializing {@link MicronautLambdaHandler}
     */
    public MicronautLambdaHandler() throws ContainerInitializationException {
        this.handler = new MicronautLambdaContainerHandler();
    }

    /**
     * Constructor.
     * @param applicationContextBuilder Application Context Builder
     * @throws ContainerInitializationException thrown intializing {@link MicronautLambdaHandler}
     */
    public MicronautLambdaHandler(ApplicationContextBuilder applicationContextBuilder) throws ContainerInitializationException {
        this.handler = new MicronautLambdaContainerHandler(applicationContextBuilder);
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest input, Context context) {
        return handler.proxy(input, context);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return this.handler.getApplicationContext();
    }

    @Override
    public void close() {
        this.getApplicationContext().close();
    }
}
