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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link ServletHttpResponse} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class ApiGatewayProxyServletResponse<B> implements ServletHttpResponse<APIGatewayProxyResponseEvent, B> {

    private final Map<String, List<String>> headers = new LinkedHashMap<>(10);
    private final ConversionService conversionService;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    private MutableConvertibleValues<Object> attributes;
    private B bodyObject;
    private int status = HttpStatus.OK.getCode();
    private String reason = HttpStatus.OK.getReason();

    public ApiGatewayProxyServletResponse(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public APIGatewayProxyResponseEvent getNativeResponse() {
        return new APIGatewayProxyResponseEvent()
            .withBody(body.toString())
            .withStatusCode(status)
            .withMultiValueHeaders(headers);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return body;
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(body, getCharacterEncoding()));
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new AwsResponseHeaders();
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

    private final class AwsResponseHeaders extends ApiGatewayMultiValueMap implements MutableHttpHeaders {

        public AwsResponseHeaders() {
            super(headers, ApiGatewayProxyServletResponse.this.conversionService);
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            if (header != null && value != null) {
                headers.computeIfAbsent(header.toString(), s -> new ArrayList<>(5)).add(value.toString());
            }
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            if (header != null) {
                headers.remove(header.toString());
            }
            return this;
        }
    }
}
