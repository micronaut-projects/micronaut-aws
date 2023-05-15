/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.aws.proxy.test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.MultiValue;
import io.micronaut.http.HttpMethod;

import jakarta.inject.Singleton;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link ServletToAwsProxyResponseAdapter}.
 *
 * @author Sergio del Amo
 */
@Singleton
public class DefaultServletToAwsProxyResponseAdapter implements ServletToAwsProxyResponseAdapter {
    @Override
    public void handle(@NonNull ConversionService conversionService,
                       @NonNull HttpServletRequest request,
                       @NonNull APIGatewayV2HTTPResponse awsProxyResponse,
                       @NonNull HttpServletResponse response) throws IOException {
        populateHeaders(conversionService, awsProxyResponse, response);
        response.setStatus(awsProxyResponse.getStatusCode());
        HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
        if (httpMethod != HttpMethod.HEAD && httpMethod != HttpMethod.OPTIONS) {

            byte[] bodyAsBytes = parseBodyAsBytes(awsProxyResponse);
            if (bodyAsBytes != null) {
                response.setContentLength(bodyAsBytes.length);
                if (bodyAsBytes.length > 0) {
                    try (OutputStream responseBody = response.getOutputStream()) {
                        responseBody.write(bodyAsBytes);
                        responseBody.flush();
                    }
                }
            }
        }
    }

    private void populateHeaders(@NonNull ConversionService conversionService,
                                 @NonNull APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse,
                                 @NonNull HttpServletResponse response) {
        Map<String, String> singleHeaders = apiGatewayV2HTTPResponse.getHeaders();
        Map<String, List<String>> multiValueHeaders = apiGatewayV2HTTPResponse.getMultiValueHeaders();
        MultiValue entries = new MultiValue(conversionService, multiValueHeaders, singleHeaders);

        for (String name: entries.names()) {
            response.addHeader(name, String.join(",", entries.getAll(name)));
        }
    }

    /**
     * Returns the response's body bytes considering whether the body was Base64 encoded.
     * @param awsProxyResponse The response
     * @return The response's body bytes.
     */
    @Nullable
    protected byte[] parseBodyAsBytes(APIGatewayV2HTTPResponse awsProxyResponse) {
        String body = awsProxyResponse.getBody();
        if (body == null) {
            return null;
        }
        return awsProxyResponse.getIsBase64Encoded() ? Base64.getMimeDecoder().decode(body) : body.getBytes(getBodyCharset());
    }

    /**
     *
     * @return The charset used to read the response's body bytes.
     */
    protected Charset getBodyCharset() {
        return StandardCharsets.UTF_8;
    }
}
