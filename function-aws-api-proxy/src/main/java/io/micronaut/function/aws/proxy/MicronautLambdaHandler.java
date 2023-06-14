/*
 * Copyright 2017-2023 original authors
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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.aws.lambda.events.APIGatewayProxyRequestEvent;
import io.micronaut.aws.lambda.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;

/**
 * @deprecated Use {@link ApiGatewayProxyRequestEventFunction} or {@link APIGatewayV2HTTPEventFunction} instead.
 */
@Deprecated(forRemoval = true, since = "4.0.0")
public class MicronautLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ApiGatewayProxyRequestEventFunction delegate;

    public MicronautLambdaHandler() {
        this.delegate = new ApiGatewayProxyRequestEventFunction();
    }

    public MicronautLambdaHandler(ApplicationContextBuilder applicationContextBuilder) {
        this.delegate = new ApiGatewayProxyRequestEventFunction(applicationContextBuilder.build());
    }

    public MicronautLambdaHandler(ApplicationContext applicationContext) {
        this.delegate = new ApiGatewayProxyRequestEventFunction(applicationContext);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        return delegate.handleRequest(input, context);
    }
}
