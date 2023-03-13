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
package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapts between a {@link APIGatewayProxyResponseEvent} to a {@link MutableHttpResponse}.
 *
 * @param <T> The HTTP Message body
 */
public class ApiGatewayProxyResponseEventAdapter<T> implements MutableHttpResponse<T> {

    private APIGatewayProxyResponseEvent event;
    private final ConversionService conversionService;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private Map<String, Cookie> cookies = new ConcurrentHashMap<>(2);

    private T body;
    private int status = HttpStatus.OK.getCode();
    private String reason = HttpStatus.OK.getReason();

    public ApiGatewayProxyResponseEventAdapter(APIGatewayProxyResponseEvent event, ConversionService conversionService) {
        this.event = event;
        this.conversionService = conversionService;
    }

    @Override
    public MutableHttpResponse<T> cookie(Cookie cookie) {
        this.cookies.put(cookie.getName(), cookie);
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new ApiGatewayProxyHeaderAdapter(event, conversionService);
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Override
    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    @Override
    public <B> MutableHttpResponse<B> body(B body) {
        this.body = (T) body;
        return (MutableHttpResponse<B>) this;
    }

    @Override
    public MutableHttpResponse<T> status(int status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        if (message == null) {
            this.reason = HttpStatus.getDefaultReason(status);
        } else {
            this.reason = message.toString();
        }
        this.status = status;
        return this;
    }

    @Override
    public int code() {
        return status;
    }

    @Override
    public String reason() {
        return reason;
    }
}
