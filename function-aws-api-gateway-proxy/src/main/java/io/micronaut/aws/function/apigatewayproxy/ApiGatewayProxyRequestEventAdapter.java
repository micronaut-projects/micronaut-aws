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

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;
import java.net.URI;
import java.util.Optional;

/**
 * Adapts between a {@link APIGatewayProxyRequestEvent} to a {@link HttpRequest}.
 *
 * @param <T> The HTTP Message body
 */
public class ApiGatewayProxyRequestEventAdapter<T> implements HttpRequest<T> {
    private APIGatewayProxyRequestEvent event;
    private final ConversionService conversionService;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();

    public ApiGatewayProxyRequestEventAdapter(ConversionService conversionService, APIGatewayProxyRequestEvent event) {
        this.conversionService = conversionService;
        this.event = event;
    }

    @Override
    public Cookies getCookies() {
        return null;
    }

    @Override
    public HttpParameters getParameters() {
        return null;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.parse(event.getHttpMethod());
    }

    @Override
    public URI getUri() {
        return URI.create(event.getPath());
    }

    @Override
    public HttpHeaders getHeaders() {
        return new ApiGatewayProxyHeaderAdapter(event, conversionService);
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Override
    public Optional<T> getBody() {
        return Optional.empty();
    }
}
