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
package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import org.jspecify.annotations.NonNull;
import io.micronaut.function.aws.HandlerUtils;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.ServletHttpHandler;

/**
 * Handles requests from API Gateway using the v2 payload format.
 * Uses comma separated header values instead of "multiValueHeaders". And puts cookies in "cookies"
 * instead of as "Set-Cookie" headers.
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Create AWS Lambda proxy integrations for HTTP APIs in API Gateway
 *     </a>
 */
public class APIGatewayV2HTTPEventFunction extends FunctionInitializer implements
    RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> httpHandler;
    private Thread shutdownHook;

    public APIGatewayV2HTTPEventFunction() {
        httpHandler = initializeHandler();
    }

    public APIGatewayV2HTTPEventFunction(ApplicationContext ctx) {
        super(ctx);
        startThis(applicationContext);
        httpHandler = initializeHandler();
    }

    private ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> initializeHandler() {
        APIGatewayV2HTTPEventHandler apiGatewayProxyEventHandler = new APIGatewayV2HTTPEventHandler(applicationContext);
        shutdownHook = new Thread(apiGatewayProxyEventHandler::close);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return apiGatewayProxyEventHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        HandlerUtils.configureWithContext(this, context);
        return httpHandler.exchange(input, new APIGatewayV2HTTPResponse()).getResponse().getNativeResponse();
    }

    @NonNull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        return new LambdaApplicationContextBuilder();
    }

    @Override
    public void close() {
        removeShutdownHook();
        if (closeContext) {
            super.close();
        } else {
            httpHandler.close();
        }
    }

    private void removeShutdownHook() {
        Thread hook = shutdownHook;
        if (hook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (IllegalStateException ignored) {
                // The JVM is already shutting down, so the hook cannot be removed.
            }
            shutdownHook = null;
        }
    }
}
