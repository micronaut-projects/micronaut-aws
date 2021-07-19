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

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.Headers;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link ServletToAwsProxyResponseAdapter}.
 *
 * @author Sergio del Amo
 */
@Singleton
public class DefaultServletToAwsProxyResponseAdapter implements ServletToAwsProxyResponseAdapter {
    @Override
    public void handle(@NonNull HttpServletRequest request,
                       @NonNull AwsProxyResponse awsProxyResponse,
                       @NonNull HttpServletResponse response) throws IOException {
        Headers responseHeaders = awsProxyResponse.getMultiValueHeaders();

        responseHeaders.forEach((key, strings) -> {
            for (String string : strings) {
                response.addHeader(key, string);
            }
        });
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

    /**
     * Returns the response's body bytes considering whether the body was Base64 encoded.
     * @param awsProxyResponse The response
     * @return The response's body bytes.
     */
    @Nullable
    protected byte[] parseBodyAsBytes(AwsProxyResponse awsProxyResponse) {
        String body = awsProxyResponse.getBody();
        return body == null ? null :
                awsProxyResponse.isBase64Encoded() ? Base64.getDecoder().decode(body) : body.getBytes(getBodyCharset());
    }

    /**
     *
     * @return The charset used to read the response's body bytes.
     */
    protected Charset getBodyCharset() {
        return StandardCharsets.UTF_8;
    }
}
