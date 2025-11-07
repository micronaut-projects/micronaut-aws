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
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.function.aws.proxy.AbstractServletHttpResponse;
import io.micronaut.function.aws.proxy.MapCollapseUtils;
import io.micronaut.function.aws.proxy.encoding.EncodingService;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ServletHttpResponse} for AWS API Gateway Proxy.
 *
 * @param <B> The body type
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
public class APIGatewayV2HTTPResponseServletResponse<B> extends AbstractServletHttpResponse<APIGatewayV2HTTPResponse, B> {
    private final APIGatewayV2HTTPEvent request;
    private final ConversionService conversionService;

    protected APIGatewayV2HTTPResponseServletResponse(ConversionService conversionService,
                                                      BinaryTypeConfiguration binaryTypeConfiguration,
                                                      EncodingService encodingService,
                                                      APIGatewayV2HTTPEvent request) {
        super(conversionService, binaryTypeConfiguration, encodingService);
        this.request = request;
        this.conversionService = conversionService;
    }

    @Override
    public APIGatewayV2HTTPResponse getNativeResponse() {
        APIGatewayV2HTTPResponse.APIGatewayV2HTTPResponseBuilder apiGatewayV2HTTPResponseBuilder = APIGatewayV2HTTPResponse.builder();

        boolean isMediaTypeBinary = binaryTypeConfiguration
            .isMediaTypeBinary(getHeaders().getContentType().orElse(null));

        Map<String, List<String>> requestHeaders = request.getHeaders().entrySet().stream()
            .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue().split(","))
                .toList()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        HttpHeaders entries =
            new CaseInsensitiveMutableHttpHeaders(requestHeaders, conversionService);

        // Apply compression if needed (i.e.: gzip/deflate)
        boolean isBodyCompressed = isBodyCompressed(entries);
        byte[] compressedBody = compressBody(entries, getHeaders(), body.toByteArray());

        if (isMediaTypeBinary || isBodyCompressed) {
            apiGatewayV2HTTPResponseBuilder
                .withIsBase64Encoded(true)
                .withBody(Base64.getEncoder().encodeToString(compressedBody));
        } else {
            String bodyStr = body.toString(getCharacterEncoding());
            if (StringUtils.isNotEmpty(bodyStr)) {
                apiGatewayV2HTTPResponseBuilder.withBody(bodyStr);
            }
        }
        apiGatewayV2HTTPResponseBuilder
            .withHeaders(MapCollapseUtils.getSingleValueHeaders(headers))
            .withMultiValueHeaders(MapCollapseUtils.getMultiHeaders(headers))
            .withStatusCode(status);

        return apiGatewayV2HTTPResponseBuilder.build();
    }

}
