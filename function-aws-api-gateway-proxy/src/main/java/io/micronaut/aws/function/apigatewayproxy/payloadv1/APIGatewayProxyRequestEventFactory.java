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
package io.micronaut.aws.function.apigatewayproxy.payloadv1;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class APIGatewayProxyRequestEventFactory {

    private APIGatewayProxyRequestEventFactory() {

    }

    public static APIGatewayProxyRequestEvent create(HttpRequest<?> request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        request.getHeaders().forEach(headers::put);
        request.getParameters().forEach(parameters::put);
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
                return request.getHeaders().asMap(String.class, String.class);
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