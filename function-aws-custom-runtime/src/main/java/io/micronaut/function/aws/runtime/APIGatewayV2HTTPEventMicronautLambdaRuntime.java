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

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;

/**
 * Main entry for AWS API proxy with Micronaut.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class APIGatewayV2HTTPEventMicronautLambdaRuntime extends AbstractMicronautLambdaRuntime<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse, APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    protected RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> createRequestHandler(String... args) {
        return new APIGatewayV2HTTPEventFunction(createApplicationContextBuilderWithArgs(args).build());
    }

    /**
     *
     * @param args Command Line args
     */
    public static void main(String[] args) throws Exception {
        new APIGatewayV2HTTPEventMicronautLambdaRuntime().run(args);
    }
}
