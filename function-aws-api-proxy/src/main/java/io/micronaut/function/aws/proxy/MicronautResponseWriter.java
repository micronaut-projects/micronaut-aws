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
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.exceptions.InvalidResponseObjectException;
import com.amazonaws.serverless.proxy.ResponseWriter;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.Writable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.proxy.cookie.ServerCookieEncoder;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link ResponseWriter} class for Micronaut.
 *
 * @author graemerocher
 * @since 1.1
 */
@Internal
public class MicronautResponseWriter extends ResponseWriter<MicronautAwsProxyResponse<?>, AwsProxyResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(MicronautResponseWriter.class);
    private static final String TIMER_NAME = "MICRONAUT_RESPONSE_WRITE";
    private final MicronautLambdaContainerContext lambdaContainerContext;

    /**
     * Default constructor.
     * @param lambdaContainerContext The {@link MicronautLambdaContainerContext}
     */
    MicronautResponseWriter(MicronautLambdaContainerContext lambdaContainerContext) {
        this.lambdaContainerContext = lambdaContainerContext;
    }

    @Override
    public AwsProxyResponse writeResponse(
            MicronautAwsProxyResponse<?> containerResponse,
            Context lambdaContext) throws InvalidResponseObjectException {
        Timer.start(TIMER_NAME);
        AwsProxyResponse awsProxyResponse = containerResponse.getAwsResponse();
        final Map<String, Cookie> cookies = containerResponse.getAllCookies();
        if (CollectionUtils.isNotEmpty(cookies)) {
            final List<String> values = ServerCookieEncoder.LAX.encode(cookies.values());
            final MutableHttpHeaders headers = containerResponse.getHeaders();
            for (String value : values) {
                headers.add(HttpHeaders.SET_COOKIE, value);
            }
        }
        // the unencoded body
        final Object body = containerResponse.body();
        if (body instanceof CharSequence) {
            awsProxyResponse.setBody(body.toString());
        } else if (body instanceof Writable) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writable writable = (Writable) body;
            try {
                Charset characterEncoding = containerResponse.getCharacterEncoding();
                writable.writeTo(outputStream, characterEncoding);
                if (containerResponse.isBinary(containerResponse.getContentType().map(Object::toString).orElse(null))) {
                    byte[] bytes = outputStream.toByteArray();
                    awsProxyResponse.setBody(
                            Base64.getMimeEncoder().encodeToString(bytes)
                    );
                    awsProxyResponse.setBase64Encoded(true);
                } else {
                    String output = new String(outputStream.toByteArray(), characterEncoding);
                    awsProxyResponse.setBody(output);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        } else if (body != null) {
            awsProxyResponse.setBody(
                    containerResponse.encodeBody()
            );
        }

        if (containerResponse.getAwsProxyRequest().getRequestSource() == AwsProxyRequest.RequestSource.ALB) {
            final HttpStatus status = containerResponse.getStatus();
            awsProxyResponse.setStatusDescription(
                    status + " " + status.getReason());
        }

        Timer.stop(TIMER_NAME);
        return awsProxyResponse;
    }
}
