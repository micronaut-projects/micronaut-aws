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
import com.sun.net.httpserver.HttpHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.uri.QueryStringDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Internal
class AwsProxyHttpHandler implements HttpHandler {
    APIGatewayV2HTTPEventFunction handler;
    private final ContextProvider contextProvider;

    AwsProxyHttpHandler(APIGatewayV2HTTPEventFunction handler) {
        this.handler = handler;
        ApplicationContext ctx = handler.getApplicationContext();
        this.contextProvider = ctx.getBean(ContextProvider.class);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        APIGatewayV2HTTPEvent awsProxyRequest = createAwsProxyRequest(httpExchange);
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

    private APIGatewayV2HTTPEvent createAwsProxyRequest(HttpExchange httpExchange) {
        final boolean isBase64Encoded = true;
        return new APIGatewayV2HTTPEvent() {
            private String body;

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> result = new HashMap<>();
                Set<String> headerNames = httpExchange.getRequestHeaders().keySet();

                for (String headerName : headerNames) {
                    List<String> values = httpExchange.getRequestHeaders().get(headerName);
                    result.put(headerName, String.join(",", values));
                }
                return result;
            }

            @Override
            public List<String> getCookies() {
                return httpExchange.getRequestHeaders().get(HttpHeaders.COOKIE);
            }

            private Optional<String> firstHeaderValue(String headerName) {
                List<String> headerValues = httpExchange.getRequestHeaders().get(headerName);
                if (CollectionUtils.isEmpty(headerValues)) {
                    return Optional.empty();
                }
                return Optional.of(headerValues.get(0));
            }

            @Override
            public Map<String, String> getQueryStringParameters() {
                Map<String, List<String>> parameters = new QueryStringDecoder(httpExchange.getRequestURI()).parameters();
                Map<String, String> queryParams = new HashMap<>();
                for (String k : parameters.keySet()) {
                    queryParams.put(k, String.join(",", parameters.get(k)));
                }
                return queryParams;
            }

            @Override
            public RequestContext getRequestContext() {
                RequestContext.Http.HttpBuilder httpBuilder = RequestContext.Http.builder()
                    .withPath(httpExchange.getRequestURI().getPath())
                    .withMethod(httpExchange.getRequestMethod())
                    .withProtocol(httpExchange.getProtocol());
                firstHeaderValue(HttpHeaders.USER_AGENT).ifPresent(httpBuilder::withUserAgent);
                return RequestContext.builder()
                    .withHttp(httpBuilder.build())
                    .build();
            }

            @Override
            public boolean getIsBase64Encoded() {
                return isBase64Encoded;
            }

            @Override
            public String getBody() {
                if (body == null) {
                    HttpMethod httpMethod = HttpMethod.parse(httpExchange.getRequestMethod());
                    if (HttpMethod.permitsRequestBody(httpMethod)) {
                        try (InputStream requestBody = httpExchange.getRequestBody()) {
                            byte[] data = requestBody.readAllBytes();
                            if (isBase64Encoded) {
                                body = Base64.getEncoder().encodeToString(data);
                            }
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                return body;
            }
        };

    }
}
