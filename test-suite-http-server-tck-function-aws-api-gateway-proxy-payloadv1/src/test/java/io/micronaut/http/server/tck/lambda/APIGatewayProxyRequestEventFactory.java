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
package io.micronaut.http.server.tck.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating {@link APIGatewayProxyRequestEvent} v1 instances from {@link HttpRequest} instances.
 */
@Internal
public final class APIGatewayProxyRequestEventFactory {

    private APIGatewayProxyRequestEventFactory() {
    }

    public static APIGatewayProxyRequestEvent create(HttpRequest<?> request) {
        try {
            Cookies cookies = request.getCookies();
            cookies.forEach((s, cookie) -> {
            });
        } catch (UnsupportedOperationException e) {
            //not all request types support retrieving cookies
        }
        return new APIGatewayProxyRequestEvent() {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> result = new HashMap<>();
                for (String headerName : request.getHeaders().names()) {
                    result.put(headerName, request.getHeaders().get(headerName));
                }
                return result;
            }

            @Override
            public Map<String, String> getQueryStringParameters() {
                Map<String, String> result = new HashMap<>();
                for (String paramName : request.getParameters().names()) {
                    result.put(paramName, request.getParameters().get(paramName));
                }
                return result;
            }

            @Override
            public Map<String, List<String>> getMultiValueQueryStringParameters() {
                Map<String, List<String>> result = new HashMap<>();
                for (String paramName : request.getParameters().names()) {
                    result.put(paramName, request.getParameters().getAll(paramName));
                }
                return result;
            }

            @Override
            public String getPath() {
                return request.getPath();
            }

            @Override
            public String getHttpMethod() {
                return request.getMethodName();
            }

            @Override
            public String getBody() {
                return request.getBody(Argument.of(String.class)).orElse(null);
            }
        };

    }
}
