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
package io.micronaut.aws.function.apigatewayproxy.payload1;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayServletRequest;
import io.micronaut.aws.function.apigatewayproxy.AwsCookies;
import io.micronaut.aws.function.apigatewayproxy.MapCollapseUtils;
import io.micronaut.aws.function.apigatewayproxy.MultiValueMutableHttpParameters;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class ApiGatewayProxyServletRequest<B> extends ApiGatewayServletRequest<B, APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final APIGatewayProxyRequestEvent requestEvent;
    private final ApiGatewayProxyServletResponse<?> response;
    private final HttpMethod method;
    private URI uri;
    private Cookies cookies;

    private MutableConvertibleValues<Object> attributes;

    public ApiGatewayProxyServletRequest(
        APIGatewayProxyRequestEvent requestEvent,
        ApiGatewayProxyServletResponse<Object> response,
        MediaTypeCodecRegistry codecRegistry,
        ConversionService conversionService
    ) {
        super(conversionService, codecRegistry);
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

    @NonNull
    @Override
    public Cookies getCookies() {
        Cookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    cookies = new AwsCookies(getPath(), getHeaders(), conversionService);
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
    @SuppressWarnings("unchecked")
    public ServletHttpRequest<APIGatewayProxyRequestEvent, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServletHttpResponse<APIGatewayProxyResponseEvent, ?> getResponse() {
        return response;
    }
}
