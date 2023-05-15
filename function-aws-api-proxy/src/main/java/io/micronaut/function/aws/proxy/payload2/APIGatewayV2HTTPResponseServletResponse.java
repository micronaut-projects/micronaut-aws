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
package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.function.aws.proxy.GatewayContentHelpers;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.servlet.http.ServletHttpResponse;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.Optional;

/**
 * Implementation of {@link ServletHttpResponse} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class APIGatewayV2HTTPResponseServletResponse<B> implements ServletHttpResponse<APIGatewayV2HTTPResponse, B> {

    private final MutableHttpHeaders headers;
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    private MutableConvertibleValues<Object> attributes;
    private B bodyObject;
    private int status = HttpStatus.OK.getCode();
    private String reason = HttpStatus.OK.getReason();

    public APIGatewayV2HTTPResponseServletResponse(ConversionService conversionService) {
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
    }

    @Override
    public APIGatewayV2HTTPResponse getNativeResponse() {
        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder apiGatewayV2HTTPResponseBuilder = APIGatewayV2HTTPResponse.builder()
            .withHeaders(MapCollapseUtils.getSingleValueHeaders(headers))
            .withMultiValueHeaders(MapCollapseUtils.getMulitHeaders(headers))
            .withStatusCode(status);

        if (GatewayContentHelpers.isBinary(getHeaders().getContentType().orElse(null))) {
            apiGatewayV2HTTPResponseBuilder.withIsBase64Encoded(true)
                .withBody(Base64.getMimeEncoder().encodeToString(body.toByteArray()));
        } else {
            apiGatewayV2HTTPResponseBuilder.withBody(body.toString(getCharacterEncoding()));
        }

        return apiGatewayV2HTTPResponseBuilder.build();
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
        if (cookie instanceof NettyCookie nettyCookie) {
            final String encoded = ServerCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie());
            header(HttpHeaders.SET_COOKIE, encoded);
        }
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
        if (body instanceof CharSequence && getContentType().isEmpty()) {
            contentType(MediaType.TEXT_PLAIN_TYPE);
        }
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
