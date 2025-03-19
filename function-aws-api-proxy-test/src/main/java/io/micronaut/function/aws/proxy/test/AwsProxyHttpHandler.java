/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.function.aws.proxy.test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.sun.net.httpserver.HttpExchange;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import io.micronaut.http.HttpHeaders;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Internal
class AwsProxyHttpHandler implements HttpHandlerApplicationContextAware {
    APIGatewayV2HTTPEventFunction handler;
    private final HttpExchangeToAwsProxyRequestAdapter requestAdapter;
    private final ContextProvider contextProvider;

    AwsProxyHttpHandler(APIGatewayV2HTTPEventFunction handler) {
        this.handler = handler;
        ApplicationContext ctx = handler.getApplicationContext();
        this.contextProvider = ctx.getBean(ContextProvider.class);
        this.requestAdapter = ctx.getBean(HttpExchangeToAwsProxyRequestAdapter.class);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        APIGatewayV2HTTPEvent awsProxyRequest = requestAdapter.createAwsProxyRequest(httpExchange);
        APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = handler.handleRequest(awsProxyRequest, contextProvider.getContext());
        String payload = apiGatewayV2HTTPResponse.getBody();
        String contentLengthObject = apiGatewayV2HTTPResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH);
        int contentLength = StringUtils.isNotEmpty(contentLengthObject) ? Integer.parseInt(contentLengthObject) : 0;
        for (String headerName : apiGatewayV2HTTPResponse.getHeaders().keySet()) {
            String headerValue = apiGatewayV2HTTPResponse.getHeaders().get(headerName);
            List<String> headerValues = List.of(headerValue.split(","));
            httpExchange.getResponseHeaders().put(headerName, StringUtils.isEmpty(headerValue) ? Collections.emptyList() : headerValues);
        }
        httpExchange.sendResponseHeaders(apiGatewayV2HTTPResponse.getStatusCode(), contentLength);
        if (StringUtils.isNotEmpty(payload)) {
            final OutputStream output = httpExchange.getResponseBody();
            byte[] payloadBytes = payload.getBytes();
            if (apiGatewayV2HTTPResponse.getIsBase64Encoded())  {
                payloadBytes = Base64.getDecoder().decode(payloadBytes);
            }
            output.write(payloadBytes);
            output.flush();
        }
        httpExchange.close();
    }

    @Override
    public @NonNull ApplicationContext getApplicationContext() {
        return handler.getApplicationContext();
    }
}
