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
package io.micronaut.function.aws.proxy.payload1;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
import io.micronaut.function.aws.proxy.MapListOfStringAndMapStringMutableHttpParameters;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ByteArrayByteBuffer;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class ApiGatewayProxyServletRequest<B> extends ApiGatewayServletRequest<B, APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayProxyServletRequest.class);

    private final ApiGatewayProxyServletResponse<?> response;

    public ApiGatewayProxyServletRequest(
        APIGatewayProxyRequestEvent requestEvent,
        ApiGatewayProxyServletResponse<Object> response,
        MediaTypeCodecRegistry codecRegistry,
        ConversionService conversionService,
        BodyBuilder bodyBuilder
    ) {
        super(
            conversionService,
            codecRegistry,
            requestEvent,
            URI.create(requestEvent.getPath()),
            parseMethod(requestEvent),
            LOG,
            bodyBuilder
        );
        this.response = response;
    }

    private static HttpMethod parseMethod(APIGatewayProxyRequestEvent requestEvent) {
        try {
            return HttpMethod.valueOf(requestEvent.getHttpMethod());
        } catch (IllegalArgumentException e) {
            return HttpMethod.CUSTOM;
        }
    }

    @Override
    public byte[] getBodyBytes() throws IOException {
        String body = requestEvent.getBody();
        if (StringUtils.isEmpty(body)) {
            throw new IOException("Empty Body");
        }
        Boolean isBase64Encoded = requestEvent.getIsBase64Encoded();
        return Boolean.TRUE.equals(isBase64Encoded) ? Base64.getDecoder().decode(body) : body.getBytes(getCharacterEncoding());
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(requestEvent.getMultiValueHeaders(), requestEvent.getHeaders()), conversionService);
    }

    @Override
    public MutableHttpParameters getParameters() {
        MediaType mediaType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        if (isFormSubmission(mediaType)) {
            return getParametersFromBody(requestEvent.getQueryStringParameters());
        } else {
            return new MapListOfStringAndMapStringMutableHttpParameters(conversionService, requestEvent.getMultiValueQueryStringParameters(), requestEvent.getQueryStringParameters());
        }
    }

    @Override
    public ServletHttpResponse<APIGatewayProxyResponseEvent, ?> getResponse() {
        return response;
    }
}
