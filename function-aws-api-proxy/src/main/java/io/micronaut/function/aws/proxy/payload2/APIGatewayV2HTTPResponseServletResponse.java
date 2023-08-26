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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.AbstractServletHttpResponse;
import io.micronaut.function.aws.proxy.BinaryContentConfiguration;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
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
public class APIGatewayV2HTTPResponseServletResponse<B> extends AbstractServletHttpResponse<APIGatewayV2HTTPResponse, B> {

    protected APIGatewayV2HTTPResponseServletResponse(ConversionService conversionService, BinaryContentConfiguration binaryContentConfiguration) {
        super(conversionService, binaryContentConfiguration);
    }

    @Override
    public APIGatewayV2HTTPResponse getNativeResponse() {
        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder apiGatewayV2HTTPResponseBuilder = APIGatewayV2HTTPResponse.builder()
            .withHeaders(MapCollapseUtils.getSingleValueHeaders(headers))
            .withMultiValueHeaders(MapCollapseUtils.getMultiHeaders(headers))
            .withStatusCode(status);

        if (binaryContentConfiguration.isBinary(getHeaders().getContentType().orElse(null))) {
            apiGatewayV2HTTPResponseBuilder
                .withIsBase64Encoded(true)
                .withBody(Base64.getMimeEncoder().encodeToString(body.toByteArray()));
        } else {
            apiGatewayV2HTTPResponseBuilder.withBody(body.toString(getCharacterEncoding()));
        }

        return apiGatewayV2HTTPResponseBuilder.build();
    }

}
