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

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

/**
 * Implementation of {@link ServletHttpRequest} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public final class APIGatewayV2HTTPEventServletRequest<B> extends ApiGatewayServletRequest<B, APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(APIGatewayV2HTTPEventServletRequest.class);

    private final APIGatewayV2HTTPResponseServletResponse<Object> response;

    public APIGatewayV2HTTPEventServletRequest(
        APIGatewayV2HTTPEvent requestEvent,
        APIGatewayV2HTTPResponseServletResponse<Object> response,
        ConversionService conversionService,
        BodyBuilder bodyBuilder
    ) {
        super(
            conversionService,
            requestEvent,
            URI.create(requestEvent.getRequestContext().getHttp().getPath()),
            parseMethod(() -> requestEvent.getRequestContext().getHttp().getMethod()),
            LOG,
            bodyBuilder
        );
        this.response = response;
    }

    @Override
    public byte[] getBodyBytes() throws IOException {
        return getBodyBytes(requestEvent::getBody, requestEvent::getIsBase64Encoded);
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return getHeaders(requestEvent::getHeaders, Collections::emptyMap);
    }

    @Override
    public MutableHttpParameters getParameters() {
        return getParameters(Collections::emptyMap, () -> transformCommaSeparatedValue(requestEvent.getQueryStringParameters()));
    }

    @Override
    public ServletHttpResponse<APIGatewayV2HTTPResponse, ?> getResponse() {
        return response;
    }
}
