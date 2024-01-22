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
package io.micronaut.function.aws.proxy.alb;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest;
import io.micronaut.function.aws.proxy.cookies.CookieDecoder;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Implementation of {@link ServletHttpRequest} for Application Load Balancer events.
 *
 * @param <B> The body type
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Internal
public class ApplicationLoadBalancerServletRequest<B> extends ApiGatewayServletRequest<B, ApplicationLoadBalancerRequestEvent, ApplicationLoadBalancerResponseEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLoadBalancerServletRequest.class);

    private final ApplicationLoadBalancerServletResponse<Object> response;

    public ApplicationLoadBalancerServletRequest(
        ApplicationLoadBalancerRequestEvent requestEvent,
        ApplicationLoadBalancerServletResponse<Object> response,
        ConversionService conversionService,
        CookieDecoder cookieDecoder,
        BodyBuilder bodyBuilder
    ) {
        super(
            conversionService,
                cookieDecoder,
            requestEvent,
            ApiGatewayServletRequest.buildUri(
                requestEvent.getPath(),
                requestEvent.getQueryStringParameters(),
                requestEvent.getMultiValueQueryStringParameters()
            ),
            parseMethod(requestEvent::getHttpMethod),
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
        return getHeaders(requestEvent::getHeaders, requestEvent::getMultiValueHeaders);
    }

    @Override
    public MutableHttpParameters getParameters() {
        return getParameters(requestEvent::getQueryStringParameters, requestEvent::getMultiValueQueryStringParameters);
    }

    @Override
    public ServletHttpResponse<ApplicationLoadBalancerResponseEvent, ?> getResponse() {
        return response;
    }
}
