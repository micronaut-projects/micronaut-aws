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
package io.micronaut.aws.function.apigatewayproxy.payload2;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.ServletHttpHandler;

public class ApiGatewayProxyEventFunction extends FunctionInitializer implements
    RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> httpHandler;
    public ApiGatewayProxyEventFunction() {
        httpHandler = initializeHandler();
    }

    public ApiGatewayProxyEventFunction(ApplicationContext ctx) {
        super(ctx);
        httpHandler = initializeHandler();
    }

    private ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> initializeHandler() {
        APIGatewayV2HTTPEventHandler apiGatewayProxyEventHandler = new APIGatewayV2HTTPEventHandler(applicationContext);
        Runtime.getRuntime().addShutdownHook(
            new Thread(apiGatewayProxyEventHandler::close)
        );
        return apiGatewayProxyEventHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {
        return httpHandler.exchange(input, new APIGatewayV2HTTPResponse()).getResponse().getNativeResponse();
    }
}