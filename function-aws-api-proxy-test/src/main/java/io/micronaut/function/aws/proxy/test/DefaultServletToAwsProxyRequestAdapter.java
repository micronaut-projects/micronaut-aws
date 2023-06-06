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
package io.micronaut.function.aws.proxy.test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link ServletToAwsProxyRequestAdapter}.
 *
 * @author Sergio del Amo
 */
@Internal

@Singleton
public class DefaultServletToAwsProxyRequestAdapter implements ServletToAwsProxyRequestAdapter {

    @Override
    @NonNull
    public APIGatewayV2HTTPEvent createAwsProxyRequest(@NonNull HttpServletRequest request) {
        final boolean isBase64Encoded = true;
        return new APIGatewayV2HTTPEvent() {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> result = new HashMap<>();
                Enumeration<String> headerNames = request.getHeaderNames();

                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    Enumeration<String> headerValues = request.getHeaders(headerName);
                    List<String> values = new ArrayList<>();
                    while (headerValues.hasMoreElements()) {
                        values.add(headerValues.nextElement());
                    }
                    result.put(headerName, String.join(",", values));
                }
                return result;
            }

            @Override
            public List<String> getCookies() {
                Enumeration<String> headerValues = request.getHeaders(HttpHeaders.COOKIE);
                List<String> cookies = new ArrayList<>();
                while (headerValues.hasMoreElements()) {
                    cookies.add(headerValues.nextElement());
                }
                return cookies;
            }

            private Optional<String> firstHeaderValue(String headerName) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                if (headerValues == null) {
                    return Optional.empty();
                }
                if (headerValues.hasMoreElements()) {
                    return Optional.of(headerValues.nextElement());
                }
                return Optional.empty();
            }

            @Override
            public Map<String, String> getQueryStringParameters() {
                Map<String, String[]> parameterMap = request.getParameterMap();
                Map<String, String> result = new HashMap<>();
                for (String paramterName : parameterMap.keySet()) {
                    result.put(paramterName, String.join(",", parameterMap.get(paramterName)));
                }
                return result;
            }

            @Override
            public RequestContext getRequestContext() {
                RequestContext.Http.HttpBuilder httpBuilder = RequestContext.Http.builder()
                    .withPath(request.getRequestURI())
                    .withMethod(request.getMethod())
                    .withProtocol(request.getProtocol());
                firstHeaderValue(HttpHeaders.USER_AGENT).ifPresent(httpBuilder::withUserAgent);
                return RequestContext.builder()
                    .withHttp(httpBuilder.build())
                    .build();
            }

            @Override
            public boolean getIsBase64Encoded() {
                return isBase64Encoded;
            }

            @Override
            public String getBody() {
                HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
                if (HttpMethod.permitsRequestBody(httpMethod)) {
                    try (InputStream requestBody = request.getInputStream()) {
                        byte[] data = requestBody.readAllBytes();
                        if (isBase64Encoded) {
                            return Base64.getEncoder().encodeToString(data);
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
                return null;
            }
        };
    }
}
