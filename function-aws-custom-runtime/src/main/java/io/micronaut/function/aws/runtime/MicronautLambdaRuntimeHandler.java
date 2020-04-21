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
package io.micronaut.function.aws.runtime;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler;

/**
 * AWS {@link RequestHandler} for {@link AwsProxyRequest} and {@link AwsProxyResponse}.
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
public class MicronautLambdaRuntimeHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse>, ApplicationContextProvider {

    protected final MicronautLambdaContainerHandler handler;

    /**
     * Constructor.
     * @throws ContainerInitializationException thrown intializing {@link MicronautLambdaRuntimeHandler}
     */
    public MicronautLambdaRuntimeHandler() throws ContainerInitializationException {
        this.handler = new MicronautLambdaContainerHandler();
    }

    /**
     * Constructor.
     * @param applicationContextBuilder Application Context Builder
     * @throws ContainerInitializationException thrown intializing {@link MicronautLambdaRuntimeHandler}
     */
    public MicronautLambdaRuntimeHandler(ApplicationContextBuilder applicationContextBuilder) throws ContainerInitializationException {
        this.handler = new MicronautLambdaContainerHandler();
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest input, Context context) {
        return handler.proxy(input, context);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return this.handler.getApplicationContext();
    }
}
