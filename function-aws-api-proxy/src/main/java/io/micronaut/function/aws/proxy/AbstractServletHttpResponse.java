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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.ServerCookieEncoder;
import io.micronaut.servlet.http.ServletHttpResponse;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

/**
 * Abstract class for implementations of {@link ServletHttpResponse}.
 * @author Sergio del Amo
 * @since 4.0.0
 * @param <R> Response Type
 * @param <B> Body Type
 */
@Internal
public abstract class AbstractServletHttpResponse<R, B> implements ServletHttpResponse<R, B> {
    protected final ByteArrayOutputStream body = new ByteArrayOutputStream();
    protected int status = HttpStatus.OK.getCode();
    protected  final MutableHttpHeaders headers;
    protected final BinaryTypeConfiguration binaryTypeConfiguration;
    private MutableConvertibleValues<Object> attributes;
    private B bodyObject;
    private String reason = HttpStatus.OK.getReason();

    protected AbstractServletHttpResponse(ConversionService conversionService, BinaryTypeConfiguration binaryTypeConfiguration) {
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
        this.binaryTypeConfiguration = binaryTypeConfiguration;
    }

    @Override
    public OutputStream getOutputStream() {
        return body;
    }

    @Override
    public BufferedWriter getWriter() {
        return new BufferedWriter(new OutputStreamWriter(body, getCharacterEncoding()));
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        ServerCookieEncoder.INSTANCE.encode(cookie)
            .forEach(c -> header(HttpHeaders.SET_COOKIE, c));
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> localAttributes = this.attributes;
        if (localAttributes == null) {
            synchronized (this) { // double check
                localAttributes = this.attributes;
                if (localAttributes == null) {
                    localAttributes = new MutableConvertibleValuesMap<>();
                    this.attributes = localAttributes;
                }
            }
        }
        return localAttributes;
    }

    @Override
    public Optional<B> getBody() {
        return Optional.ofNullable(bodyObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MutableHttpResponse<T> body(@Nullable T body) {
        this.bodyObject = (B) body;
        return (MutableHttpResponse<T>) this;
    }

    @Override
    public MutableHttpResponse<B> status(int status, CharSequence message) {
        this.status = status;
        if (message == null) {
            this.reason = HttpStatus.getDefaultReason(status);
        } else {
            this.reason = message.toString();
        }
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
