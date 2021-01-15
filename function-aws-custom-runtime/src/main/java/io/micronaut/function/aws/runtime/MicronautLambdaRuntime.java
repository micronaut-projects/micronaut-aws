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
package io.micronaut.function.aws.runtime;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;

import java.net.MalformedURLException;

/**
 * Main entry for AWS API proxy with Micronaut.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class MicronautLambdaRuntime extends AbstractMicronautLambdaRuntime<AwsProxyRequest, AwsProxyResponse, AwsProxyRequest, AwsProxyResponse> {

    @Override
    protected RequestHandler<AwsProxyRequest, AwsProxyResponse> createRequestHandler(String... args) {
        try {
            return new MicronautLambdaHandler(createApplicationContextBuilderWithArgs(args));
        } catch (ContainerInitializationException e) {
            throw new ConfigurationException("Exception thrown instantiating MicronautLambdaRuntimeHandler", e);
        }
    }

    /**
     *
     * @param args Command Line args
     */
    public static void main(String[] args) {
        try {
            new MicronautLambdaRuntime().run(args);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
