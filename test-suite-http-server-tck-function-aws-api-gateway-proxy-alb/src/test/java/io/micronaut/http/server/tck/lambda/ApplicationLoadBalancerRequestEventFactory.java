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

import io.micronaut.aws.lambda.events.ApplicationLoadBalancerRequestEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.function.aws.proxy.test.BodyUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.json.JsonMapper;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating {@link ApplicationLoadBalancerRequestEvent} instances from {@link HttpRequest} instances.
 */
@Internal
public final class ApplicationLoadBalancerRequestEventFactory {

    private ApplicationLoadBalancerRequestEventFactory() {
    }

    @NonNull
    public static ApplicationLoadBalancerRequestEvent create(@NonNull HttpRequest<?> request, JsonMapper jsonMapper) {
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, List<String>> multiHeaders = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if (values.size() > 1) {
                multiHeaders.put(name, values);
            } else {
                headers.put(name, values.get(0));
            }
        });
        try {
            Cookies cookies = request.getCookies();
            boolean many = cookies.getAll().size() > 1;
            cookies.forEach((s, cookie) -> {
                if (cookie instanceof NettyCookie nettyCookie) {
                    if (many) {
                        multiHeaders.computeIfAbsent(HttpHeaders.COOKIE, s1 -> new ArrayList<>())
                            .add(ClientCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie()));
                    } else {
                        headers.put(HttpHeaders.COOKIE, ClientCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie()));
                    }
                }
            });
        } catch (UnsupportedOperationException e) {
            //not all request types support retrieving cookies
        }
        return new ApplicationLoadBalancerRequestEvent() {

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public Map<String, List<String>> getMultiValueHeaders() {
                return multiHeaders;
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
                return request.getBody()
                    .flatMap(b -> BodyUtils.bodyAsString(jsonMapper,
                        () -> request.getContentType().orElse(null),
                        request::getCharacterEncoding,
                        () -> b)
                    ).orElse(null);
            }
        };
    }
}
