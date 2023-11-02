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
package io.micronaut.function.aws.proxy.payload1;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.function.aws.HandlerUtils;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.ServletHttpHandler;

/**
 * Handles requests from API Gateway using the v1 payload format.
 */
public class ApiGatewayProxyRequestEventFunction extends FunctionInitializer implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ServletHttpHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> httpHandler;

    public ApiGatewayProxyRequestEventFunction() {
        httpHandler = initializeHandler();
    }

    public ApiGatewayProxyRequestEventFunction(ApplicationContext ctx) {
        super(ctx);
        startThis(applicationContext);
        httpHandler = initializeHandler();
    }

    private ServletHttpHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> initializeHandler() {
        ApiGatewayProxyEventHandler apiGatewayProxyEventHandler = new ApiGatewayProxyEventHandler(applicationContext);
        Runtime.getRuntime().addShutdownHook(
            new Thread(apiGatewayProxyEventHandler::close)
        );
        return apiGatewayProxyEventHandler;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        HandlerUtils.configureWithContext(this, context);
        return httpHandler.exchange(input, new APIGatewayProxyResponseEvent()).getResponse().getNativeResponse();
    }

    @NonNull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return new LambdaApplicationContextBuilder();
    }
}
