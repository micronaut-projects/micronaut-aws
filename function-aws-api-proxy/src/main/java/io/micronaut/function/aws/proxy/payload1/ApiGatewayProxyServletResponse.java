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

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.function.aws.proxy.AbstractServletHttpResponse;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
import io.micronaut.function.aws.proxy.cookies.CookieEncoder;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.util.Base64;

/**
 * Implementation of {@link ServletHttpResponse} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class ApiGatewayProxyServletResponse<B> extends AbstractServletHttpResponse<APIGatewayProxyResponseEvent, B> {

    protected ApiGatewayProxyServletResponse(ConversionService conversionService,
                                             BinaryTypeConfiguration binaryTypeConfiguration,
                                             CookieEncoder cookieEncoder
                                             ) {
        super(conversionService, binaryTypeConfiguration, cookieEncoder);
    }

    @Override
    public APIGatewayProxyResponseEvent getNativeResponse() {
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent()
            .withBody(body.toString())
            .withStatusCode(status)
            .withMultiValueHeaders(MapCollapseUtils.getMultiHeaders(headers))
            .withHeaders(MapCollapseUtils.getSingleValueHeaders(headers));

        if (binaryTypeConfiguration.isMediaTypeBinary(getHeaders().getContentType().orElse(null))) {
            apiGatewayProxyResponseEvent
                .withIsBase64Encoded(true)
                .withBody(Base64.getMimeEncoder().encodeToString(body.toByteArray()));
        } else {
            apiGatewayProxyResponseEvent
                .withIsBase64Encoded(false)
                .withBody(body.toString(getCharacterEncoding()));
        }
        return apiGatewayProxyResponseEvent;
    }
}
