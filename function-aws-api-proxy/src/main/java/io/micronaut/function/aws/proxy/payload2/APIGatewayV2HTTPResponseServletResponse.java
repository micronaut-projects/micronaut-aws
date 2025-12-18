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
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.function.aws.proxy.AbstractServletHttpResponse;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ServletHttpResponse} for AWS API Gateway Proxy.
 * Uses comma separated header values instead of "multiValueHeaders". And puts cookies in "cookies"
 * instead of as "Set-Cookie" headers.
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Create AWS Lambda proxy integrations for HTTP APIs in API Gateway
 *     </a>
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class APIGatewayV2HTTPResponseServletResponse<B> extends AbstractServletHttpResponse<APIGatewayV2HTTPResponse, B> {

    protected APIGatewayV2HTTPResponseServletResponse(ConversionService conversionService, BinaryTypeConfiguration binaryTypeConfiguration) {
        super(conversionService, binaryTypeConfiguration);
    }

    @Override
    public APIGatewayV2HTTPResponse getNativeResponse() {
        Map<String, List<String>> multiValueHeaders = MapCollapseUtils.getMultiHeaders(headers);
        List<String> cookies = multiValueHeaders.get(HttpHeaders.SET_COOKIE);
        multiValueHeaders.remove(HttpHeaders.SET_COOKIE);

        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder apiGatewayV2HTTPResponseBuilder = APIGatewayV2HTTPResponse.builder()
            .withHeaders(MapCollapseUtils.collapse(multiValueHeaders))
            //.withMultiValueHeaders(multiValueHeaders)
            .withCookies(cookies)
            .withStatusCode(status);
        List<String> cookies = headers.getAll(HttpHeaders.SET_COOKIE);
        if (CollectionUtils.isNotEmpty(cookies)) {
            apiGatewayV2HTTPResponseBuilder.withCookies(cookies);
        }
        if (binaryTypeConfiguration.isMediaTypeBinary(getHeaders().getContentType().orElse(null))) {
            apiGatewayV2HTTPResponseBuilder
                .withIsBase64Encoded(true)
                .withBody(Base64.getEncoder().encodeToString(body.toByteArray()));
        } else {
            String bodyStr = body.toString(getCharacterEncoding());
            if (StringUtils.isNotEmpty(bodyStr)) {
                apiGatewayV2HTTPResponseBuilder.withBody(bodyStr);
            }
        }

        return apiGatewayV2HTTPResponseBuilder.build();
    }

}
