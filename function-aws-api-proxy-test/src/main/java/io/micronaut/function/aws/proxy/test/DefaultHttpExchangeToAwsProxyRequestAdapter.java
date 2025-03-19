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
import com.sun.net.httpserver.HttpExchange;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Internal
@Singleton
class DefaultHttpExchangeToAwsProxyRequestAdapter implements HttpExchangeToAwsProxyRequestAdapter {
    @Override
    public APIGatewayV2HTTPEvent createAwsProxyRequest(HttpExchange httpExchange) {
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
                URI requestURI = httpExchange.getRequestURI();
                return getQueryParams(requestURI);
            }

            public static Map<String, String> getQueryParams(URI uri) {
                Map<String, String> queryParams = new HashMap<>();
                String query = uri.getQuery();
                if (query != null) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        int idx = pair.indexOf("=");
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        queryParams.put(key, value);
                    }
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
