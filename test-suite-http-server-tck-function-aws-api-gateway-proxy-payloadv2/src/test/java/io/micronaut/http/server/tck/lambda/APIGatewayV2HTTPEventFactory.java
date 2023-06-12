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

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpVersion;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Factory for creating {@link APIGatewayV2HTTPEvent} v2 instances from {@link HttpRequest} instances.
 */
@Internal
public final class APIGatewayV2HTTPEventFactory {

    private APIGatewayV2HTTPEventFactory() {
    }

    public static APIGatewayV2HTTPEvent create(HttpRequest<?> request, JsonMapper jsonMapper) {
        Function<Object, String> maybeConvertBody = body -> {
            // Assume no content type == json
            boolean mapFromJson = request.getContentType().map(MediaType.APPLICATION_JSON_TYPE::equals).orElse(true);
            if (body instanceof CharSequence) {
                return body.toString();
            } else if (body instanceof byte[]) {
                return new String(((byte[]) body), request.getCharacterEncoding());
            } else if (mapFromJson) {
                try {
                    return jsonMapper.writeValueAsString(body);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        };
        return new APIGatewayV2HTTPEvent() {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> result = new HashMap<>();
                for (String headerName : request.getHeaders().names()) {
                    result.put(headerName, request.getHeaders().get(headerName));
                }
                return result;
            }

            @Override
            public List<String> getCookies() {
                return request.getHeaders().getAll(HttpHeaders.COOKIE);
            }

            @Override
            public Map<String, String> getQueryStringParameters() {
                Map<String, String> result = new HashMap<>();
                for (String paramName : request.getParameters().names()) {
                    result.put(paramName, String.join(",", request.getParameters().getAll(paramName)));
                }
                return result;
            }

            @Override
            public RequestContext getRequestContext() {
                RequestContext.Http.HttpBuilder httpBuilder = RequestContext.Http.builder()
                    .withMethod(request.getMethodName())
                    .withPath(request.getPath())
                    .withUserAgent(request.getHeaders().get(HttpHeaders.USER_AGENT));
                protocol(request.getHttpVersion()).ifPresent(httpBuilder::withProtocol);
                return RequestContext.builder()
                    .withHttp(httpBuilder.build())
                    .build();
            }

            @Override
            public String getBody() {
                return request.getBody().map(maybeConvertBody).orElse(null);
            }
        };
    }

    private static Optional<String> protocol(HttpVersion httpVersion) {
        if (httpVersion == HttpVersion.HTTP_1_0) {
            return Optional.of("HTTP/1.0");
        } else if (httpVersion == HttpVersion.HTTP_1_1) {
            return Optional.of("HTTP/1.1");
        } else if (httpVersion == HttpVersion.HTTP_2_0) {
            return Optional.of("HTTP/2.0");
        }
        return Optional.empty();
    }
}
