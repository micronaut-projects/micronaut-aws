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

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.aws.function.apigatewayproxy.MapCollapseUtils;
import io.micronaut.aws.function.apigatewayproxy.MultiValueMutableHttpParameters;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;
import io.micronaut.servlet.http.MutableServletHttpRequest;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class APIGatewayV2HTTPEventServletRequest<B> implements
    MutableServletHttpRequest<APIGatewayV2HTTPEvent, B>,
    ServletExchange<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final APIGatewayV2HTTPEvent requestEvent;
    private final APIGatewayV2HTTPResponseServletResponse response;
    private final MediaTypeCodecRegistry codecRegistry;
    private final HttpMethod method;
    private URI uri;
    private SimpleCookies cookies;

    private ConversionService conversionService;
    private MutableConvertibleValues<Object> attributes;

    public APIGatewayV2HTTPEventServletRequest(
        APIGatewayV2HTTPEvent requestEvent,
        APIGatewayV2HTTPResponseServletResponse<Object> response,
        MediaTypeCodecRegistry codecRegistry,
        ConversionService conversionService
    ) {
        this.requestEvent = requestEvent;
        this.uri = URI.create(requestEvent.getRequestContext().getHttp().getPath());
        HttpMethod parsedMethod;
        try {
            parsedMethod = HttpMethod.valueOf(requestEvent.getRequestContext().getHttp().getMethod());
        } catch (IllegalArgumentException e) {
            parsedMethod = HttpMethod.CUSTOM;
        }
        this.method = parsedMethod;
        this.response = response;
        this.codecRegistry = codecRegistry;
        this.conversionService = conversionService;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String body = requestEvent.getBody();
        if (StringUtils.isEmpty(body)) {
            throw new IOException("Empty Body");
        }
        return new ByteArrayInputStream(
            body.getBytes(getCharacterEncoding())
        );
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public APIGatewayV2HTTPEvent getNativeRequest() {
        return requestEvent;
    }

    @Override
    public Cookies getCookies() {
        SimpleCookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    this.cookies = new SimpleCookies(conversionService);
                }
            }
        }
        for (String cookie : requestEvent.getCookies()) {
            String[] parts = cookie.split(";");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    this.cookies.put(keyValue[0], new SimpleCookie(keyValue[0], keyValue[1]));
                }
            }
        }
        return this.cookies;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public MutableHttpRequest<B> cookie(Cookie cookie) {

        return this;
    }

    @Override
    public MutableHttpRequest<B> uri(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public <T> MutableHttpRequest<T> body(T body) {
        return null;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(Collections.emptyMap(), requestEvent.getHeaders()), conversionService);
    }

    @NonNull
    private static List<String> splitCommaSeparatedValue(@Nullable String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    @NonNull
    private static Map<String, List<String>> transformCommaSeparatedValue(@Nullable Map<String, String> input) {
        if (input == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> output = new HashMap<>();
        for (String key : input.keySet()) {
            output.put(key, splitCommaSeparatedValue(input.get(key)));
        }
        return output;

    }

    @Override
    public MutableHttpParameters getParameters() {
        return new MultiValueMutableHttpParameters(conversionService, transformCommaSeparatedValue(requestEvent.getQueryStringParameters()), Collections.emptyMap());
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> attributes = this.attributes;
        if (attributes == null) {
            synchronized (this) { // double check
                attributes = this.attributes;
                if (attributes == null) {
                    attributes = new MutableConvertibleValuesMap<>();
                    this.attributes = attributes;
                }
            }
        }
        return attributes;
    }

    @Override
    @NonNull
    public Optional<B> getBody() {
        return Optional.empty();
    }

    @NonNull
    @Override
    public <T> Optional<T> getBody(@NonNull Argument<T> arg) {
        if (arg == null) {
            return Optional.empty();
        }

        final Class<T> type = arg.getType();
        final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
        if (codec != null) {
            if (ConvertibleValues.class == type) {
                final Map map = codec.decode(Map.class, requestEvent.getBody());
                ConvertibleValues result = ConvertibleValues.of(map);
                return (Optional<T>) Optional.of(result);
            } else {
                final T value = codec.decode(arg, requestEvent.getBody());
                return Optional.ofNullable(value);
            }
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServletHttpRequest<APIGatewayV2HTTPEvent, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServletHttpResponse<APIGatewayV2HTTPResponse, ?> getResponse() {
        return response;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
