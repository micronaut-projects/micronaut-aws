/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.exceptions.InvalidResponseObjectException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.Headers;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.cookie.Cookie;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of {@link MutableHttpResponse} for AWS API proxy.
 *
 * @author graemerocher
 * @since 1.1
 * @param <T> The body type
 */
public class MicronautAwsProxyResponse<T> implements MutableHttpResponse<T>, Closeable {

    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final CountDownLatch responseEncodeLatch;
    private final AwsProxyRequest request;
    private final MicronautLambdaContainerContext handler;
    private T body;
    private HttpStatus status;
    private AwsProxyResponse response = new AwsProxyResponse();
    private final AwsHeaders awsHeaders = new AwsHeaders();
    private Headers multiValueHeaders = new Headers();
    private Map<String, Cookie> cookies = new ConcurrentHashMap<>(2);

    /**
     * The default constructor.
     * @param request The request
     * @param latch The latch to indicate request completion
     * @param environment The {@link MicronautLambdaContainerContext}
     */
    MicronautAwsProxyResponse(
            AwsProxyRequest request,
            CountDownLatch latch,
            MicronautLambdaContainerContext environment) {
        this.responseEncodeLatch = latch;
        this.request = request;
        this.response.setMultiValueHeaders(multiValueHeaders);
        this.handler = environment;
        this.status = HttpStatus.OK;
        this.response.setStatusCode(HttpStatus.OK.getCode());
    }

    @Override
    @Nonnull
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Override
    @Nonnull
    public Optional<T> getBody() {
        return Optional.ofNullable(body);
    }

    @Override
    public MutableHttpResponse<T> cookie(Cookie cookie) {
        this.cookies.put(cookie.getName(), cookie);
        return this;
    }

    @Override
    @Nonnull
    public MutableHttpHeaders getHeaders() {
        return awsHeaders;
    }

    @Override
    public MutableHttpResponse<T> body(@Nullable T body) {
        this.body = body;
        return this;
    }

    @Override
    public MutableHttpResponse<T> status(HttpStatus status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        this.status = status;
        response.setStatusCode(status.getCode());
        return this;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @return Any cookies
     */
    Map<String, Cookie> getCookies() {
        return cookies;
    }

    /**
     * @return The backing response.
     */
    AwsProxyResponse getAwsResponse() {
        return response;
    }

    /**
     * @return The backing request.
     */
    AwsProxyRequest getAwsProxyRequest() {
        return this.request;
    }

    /**
     * Encode the body.
     * @return The body encoded as a String
     * @throws InvalidResponseObjectException If the body couldn't be encoded
     */
    String encodeBody() throws InvalidResponseObjectException {
        if (body instanceof CharSequence) {
            return body.toString();
        }
        byte[] encoded = encodeInternal(handler.getJsonCodec());
        if (encoded != null) {
            final String contentType = getContentType().map(MediaType::toString).orElse(null);
            if (!isBinary(contentType)) {
                return new String(encoded, getCharacterEncoding());
            } else {
                response.setBase64Encoded(true);
                return Base64.getMimeEncoder().encodeToString(encoded);
            }
        }
        return null;
    }

    private byte[] encodeInternal(MediaTypeCodec codec) throws InvalidResponseObjectException {
        byte[] encoded = null;
        try {
            if (body != null) {
                if (body instanceof ByteBuffer) {
                    encoded = ((ByteBuffer) body).toByteArray();
                } else if (body instanceof byte[]) {
                    encoded = (byte[]) body;
                } else {
                    final Optional<MediaType> contentType = getContentType();
                    if (!contentType.isPresent()) {
                        contentType(MediaType.APPLICATION_JSON_TYPE);
                    }
                    encoded = codec.encode(body);
                }
            }
        } catch (Exception e) {
            throw new InvalidResponseObjectException("Invalid Response: " + e.getMessage() , e);
        }
        return encoded;
    }

    @Override
    public void close() {
        responseEncodeLatch.countDown();
    }

    private boolean isBinary(String contentType) {
        if (contentType != null) {
            int semidx = contentType.indexOf(';');
            if (semidx >= 0) {
                return MicronautLambdaContainerHandler.getContainerConfig().isBinaryContentType(contentType.substring(0, semidx));
            } else {
                return MicronautLambdaContainerHandler.getContainerConfig().isBinaryContentType(contentType);
            }
        }
        return false;
    }

    /**
     * An implementation of {@link MutableHttpHeaders} for AWS lambda.
     */
    private class AwsHeaders implements MutableHttpHeaders {

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            ArgumentUtils.requireNonNull("header", header);
            ArgumentUtils.requireNonNull("value", value);
            multiValueHeaders.add(header.toString(), value.toString());
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            ArgumentUtils.requireNonNull("header", header);
            multiValueHeaders.remove(header.toString());
            return this;
        }

        @Override
        public List<String> getAll(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                return multiValueHeaders.get(name.toString());
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                return multiValueHeaders.getFirst(name.toString());
            }
            return null;
        }

        @Override
        public Set<String> names() {
            return multiValueHeaders.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return multiValueHeaders.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            final String v = get(name);
            if (v != null) {
                return ConversionService.SHARED.convert(v, conversionContext);
            }
            return Optional.empty();
        }
    }
}
