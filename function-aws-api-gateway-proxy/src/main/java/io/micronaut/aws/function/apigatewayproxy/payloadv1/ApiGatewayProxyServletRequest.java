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
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.aws.function.apigatewayproxy.MapCollapseUtils;
import io.micronaut.aws.function.apigatewayproxy.MultiMutableHttpHeaders;
import io.micronaut.aws.function.apigatewayproxy.MultiValueMutableHttpParameters;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class ApiGatewayProxyServletRequest<B> implements
    MutableServletHttpRequest<APIGatewayProxyRequestEvent, B>,
    ServletExchange<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final APIGatewayProxyRequestEvent requestEvent;
    private final ApiGatewayProxyServletResponse<?> response;
    private final MediaTypeCodecRegistry codecRegistry;
    private final HttpMethod method;
    private URI uri;
    private Cookies cookies;

    private ConversionService conversionService;
    private MutableConvertibleValues<Object> attributes;

    public ApiGatewayProxyServletRequest(
        APIGatewayProxyRequestEvent requestEvent,
        ApiGatewayProxyServletResponse<Object> response,
        MediaTypeCodecRegistry codecRegistry,
        ConversionService conversionService
    ) {
        this.requestEvent = requestEvent;
        this.uri = URI.create(requestEvent.getPath());
        HttpMethod parsedMethod;
        try {
            parsedMethod = HttpMethod.valueOf(requestEvent.getHttpMethod());
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
    public APIGatewayProxyRequestEvent getNativeRequest() {
        return requestEvent;
    }

    @Override
    public Cookies getCookies() {
        Cookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    cookies = new SimpleCookies(conversionService);
                    this.cookies = cookies;
                }
            }
        }
        return cookies;
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
        return new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(requestEvent.getMultiValueHeaders(), requestEvent.getHeaders()), conversionService);
    }

    @Override
    public MutableHttpParameters getParameters() {
        return new MultiValueMutableHttpParameters(conversionService, requestEvent.getMultiValueQueryStringParameters(), requestEvent.getQueryStringParameters());
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
    public ServletHttpRequest<APIGatewayProxyRequestEvent, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServletHttpResponse<APIGatewayProxyResponseEvent, ?> getResponse() {
        return response;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    private final class AwsHeaders implements HttpHeaders {

        @Override
        public List<String> getAll(CharSequence name) {
            if (name != null && requestEvent.getMultiValueHeaders() != null) {
                return requestEvent.getMultiValueHeaders().get(name.toString());
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            if (name != null && requestEvent.getHeaders() != null) {
                return requestEvent.getHeaders().get(name.toString());
            }
            return null;
        }

        @Override
        public Set<String> names() {
            return new HashSet<>(requestEvent.getHeaders().keySet());
        }

        @Override
        public Collection<List<String>> values() {
            if (requestEvent.getMultiValueHeaders() == null) {
                return Collections.emptyList();
            }
            return requestEvent.getMultiValueHeaders().values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            if (name != null) {
                Optional<String> v = Optional.ofNullable(requestEvent.getHeaders().get(name.toString()));
                return v.flatMap(s -> conversionService.convert(s, conversionContext));
            }
            return Optional.empty();
        }
    }
}
