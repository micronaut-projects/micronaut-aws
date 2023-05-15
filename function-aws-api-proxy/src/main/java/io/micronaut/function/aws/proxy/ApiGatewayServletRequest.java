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

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.inject.ExecutionHandle;
import io.micronaut.json.codec.MapperMediaTypeCodec;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.servlet.http.MutableServletHttpRequest;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.web.router.RouteMatch;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Base class for all the versions of the Gateway events.
 *
 * @param <T> The body type
 * @param <REQ> The request event type
 * @param <RES> The response event type
 */
@Internal
public abstract class ApiGatewayServletRequest<T, REQ, RES> implements MutableServletHttpRequest<REQ, T>, ServletExchange<REQ, RES> {

    private static final Set<Class<?>> RAW_BODY_TYPES = CollectionUtils.setOf(String.class, byte[].class, ByteBuffer.class, InputStream.class);

    protected ConversionService conversionService;
    protected final REQ requestEvent;
    private URI uri;
    private final HttpMethod httpMethod;
    private Cookies cookies;
    private final MediaTypeCodecRegistry codecRegistry;
    private MutableConvertibleValues<Object> attributes;
    private Supplier<Optional<T>> body;

    protected ApiGatewayServletRequest(ConversionService conversionService, MediaTypeCodecRegistry codecRegistry, REQ request, URI uri, HttpMethod httpMethod) {
        this.conversionService = conversionService;
        this.codecRegistry = codecRegistry;
        this.requestEvent = request;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.body = SupplierUtil.memoizedNonEmpty(() -> {
            T built = (T) buildBody();
            return Optional.ofNullable(built);
        });
    }

    public abstract byte[] getBodyBytes() throws IOException;

    @Nullable
    protected Object buildBody() {
        final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        if (isFormSubmission(contentType)) {
            try {
                return new QueryStringDecoder(new String(getBodyBytes(), getCharacterEncoding()), false).parameters();
            } catch (IOException e) {
                throw new CodecException("Error decoding request body: " + e.getMessage(), e);
            }
        } else {
            if (getContentLength() == 0) {
                return null;
            }
            Argument<?> resolvedBodyType = resolveBodyType();
            try (InputStream inputStream = getInputStream())  {
                if (resolvedBodyType != null && RAW_BODY_TYPES.contains(resolvedBodyType.getType())) {
                    return inputStream.readAllBytes();
                } else {
                    final MediaTypeCodec codec = codecRegistry.findCodec(contentType).orElse(null);
                    if (contentType.equals(MediaType.APPLICATION_JSON_TYPE) && codec instanceof MapperMediaTypeCodec mapperCodec) {
                        return readJson(inputStream, mapperCodec);
                    } else if (codec != null) {
                        return decode(inputStream, codec);
                    } else {
                        return inputStream.readAllBytes();
                    }
                }
            } catch (EOFException e) {
                // no content
                return null;
            } catch (IOException e) {
                throw new CodecException("Error decoding request body: " + e.getMessage(), e);
            }
        }
    }

    private Argument<?> resolveBodyType() {
        RouteMatch<?> route = this.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch.class).orElse(null);
        if (route != null) {
            Argument<?> bodyType = route.getRouteInfo().getFullRequestBodyType()
                /*
                The getBodyArgument() method returns arguments for functions where it is
                not possible to dictate whether the argument is supposed to bind the entire
                body or just a part of the body. We check to ensure the argument has the body
                annotation to exclude that use case
                */
                .filter(argument -> {
                    AnnotationMetadata annotationMetadata = argument.getAnnotationMetadata();
                    if (annotationMetadata.hasAnnotation(Body.class)) {
                        return annotationMetadata.stringValue(Body.class).isEmpty();
                    } else {
                        return false;
                    }
                })
                .orElseGet(() -> {
                    if (route instanceof ExecutionHandle<?, ?> handle) {
                        for (Argument<?> argument : handle.getArguments()) {
                            if (argument.getType() == HttpRequest.class) {
                                return argument;
                            }
                        }
                    }
                    return Argument.OBJECT_ARGUMENT;
                });
            if (bodyType.getType() == HttpRequest.class) {
                bodyType = bodyType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
            }
            return bodyType;
        } else {
            return Argument.OBJECT_ARGUMENT;
        }
    }

    private Object decode(InputStream inputStream, MediaTypeCodec codec) throws IOException {
        return codec.decode(Argument.of(byte[].class), inputStream);
    }

    private Object readJson(InputStream inputStream, MapperMediaTypeCodec mapperCodec) throws IOException {
        return mapperCodec.getJsonMapper().readValue(inputStream, Argument.of(JsonNode.class));
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

    @NonNull
    @Override
    public Optional<T> getBody() {
        return this.body.get();
    }

    @NonNull
    @Override
    public <B> Optional<B> getBody(Argument<B> arg) {
        return getBody().map(t -> conversionService.convertRequired(t, arg));
    }

    private boolean isFormSubmission(MediaType contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
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
