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
package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.MutableServletHttpRequest;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for all the versions of the Gateway events.
 *
 * @param <T> The body type
 * @param <REQ> The request event type
 * @param <RES> The response event type
 */
@Internal
public abstract class ApiGatewayServletRequest<T, REQ, RES> implements MutableServletHttpRequest<REQ, T>, ServletExchange<REQ, RES> {

    protected Object body;
    protected ConversionService conversionService;
    protected final REQ requestEvent;
    private URI uri;
    private final HttpMethod httpMethod;
    private Cookies cookies;
    private final MediaTypeCodecRegistry codecRegistry;
    private MutableConvertibleValues<Object> attributes;

    protected ApiGatewayServletRequest(ConversionService conversionService, MediaTypeCodecRegistry codecRegistry, REQ request, URI uri, HttpMethod httpMethod) {
        this.conversionService = conversionService;
        this.codecRegistry = codecRegistry;
        this.requestEvent = request;
        this.uri = uri;
        this.httpMethod = httpMethod;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServletHttpRequest<REQ, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public REQ getNativeRequest() {
        return requestEvent;
    }

    @Override
    public HttpMethod getMethod() {
        return httpMethod;
    }

    @Override
    public URI getUri() {
        return uri;
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
    public Optional<T> getBody() {
        return (Optional<T>) getBody(Argument.OBJECT_ARGUMENT);
    }

    @Override
    public <B> Optional<B> getBody(Argument<B> arg) {
        if (arg != null) {
            final Class<B> type = arg.getType();
            final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
            if (body == null) {

                if (isFormSubmission(contentType)) {
                    body = getParameters();
                    if (ConvertibleValues.class == type || Object.class == type) {
                        return (Optional<B>) Optional.of(body);
                    } else {
                        return Optional.empty();
                    }
                } else {

                    final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
                    if (codec != null) {
                        try (InputStream inputStream = getInputStream()) {
                            if (ConvertibleValues.class == type) {
                                final Map map = codec.decode(Map.class, inputStream);
                                body = ConvertibleValues.of(map);
                                return (Optional<B>) Optional.of(body);
                            } else {
                                final B value = codec.decode(arg, inputStream);
                                body = value;
                                return Optional.ofNullable(value);
                            }
                        } catch (IOException e) {
                            throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                        }

                    }
                }
            } else {
                if (type.isInstance(body)) {
                    return (Optional<B>) Optional.of(body);
                } else {
                    final B result = conversionService.convertRequired(body, arg);
                    return Optional.ofNullable(result);
                }

            }
        }
        return Optional.empty();
    }

    private boolean isFormSubmission(MediaType contentType) {
        return MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }


    @Override
    public MutableHttpRequest<T> cookie(Cookie cookie) {
        //TODO
        return (MutableHttpRequest<T>) this;
    }

    @Override
    public MutableHttpRequest<T> uri(URI uri) {
        //TODO
        return (MutableHttpRequest<T>) this;
    }

    @Override
    public <T> MutableHttpRequest<T> body(T body) {
        //TODO
        return (MutableHttpRequest<T>) this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
