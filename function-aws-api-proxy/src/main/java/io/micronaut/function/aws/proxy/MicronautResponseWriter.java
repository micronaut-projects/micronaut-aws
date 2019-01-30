/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import javax.ws.rs.core.Response;
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

    private static final String TIMER_NAME = "MICRONAUT_RESPONSE_WRITE";
    private final LambdaContainerContext lambdaContainerContext;

    /**
     * Default constructor.
     * @param lambdaContainerContext The {@link LambdaContainerContext}
     */
    MicronautResponseWriter(LambdaContainerContext lambdaContainerContext) {
        this.lambdaContainerContext = lambdaContainerContext;
    }

    @Override
    public AwsProxyResponse writeResponse(
            MicronautAwsProxyResponse<?> containerResponse,
            Context lambdaContext) throws InvalidResponseObjectException {
        Timer.start(TIMER_NAME);
        AwsProxyResponse awsProxyResponse = containerResponse.getAwsResponse();
        final Map<String, Cookie> cookies = containerResponse.getCookies();
        if (CollectionUtils.isNotEmpty(cookies)) {
            final io.netty.handler.codec.http.cookie.Cookie[] nettyCookies = cookies.values().stream().filter(c -> c instanceof NettyCookie).map(c -> ((NettyCookie) c).getNettyCookie()).toArray(io.netty.handler.codec.http.cookie.Cookie[]::new);
            final List<String> values = ServerCookieEncoder.LAX.encode(nettyCookies);
            final MutableHttpHeaders headers = containerResponse.getHeaders();
            for (String value : values) {
                headers.add(HttpHeaders.SET_COOKIE, value);
            }
        }
        // the unencoded body
        final Object body = containerResponse.body();
        if (body instanceof CharSequence) {
            awsProxyResponse.setBody(body.toString());
        } else if (body != null) {
            awsProxyResponse.setBody(
                    containerResponse.encodeBody()
            );
        }

        if (containerResponse.getAwsProxyRequest().getRequestSource() == AwsProxyRequest.RequestSource.ALB) {
            final HttpStatus status = containerResponse.getStatus();
            awsProxyResponse.setStatusDescription(
                    status + " " +
                            Response.Status.fromStatusCode(status.getCode()).getReasonPhrase());
        }

        Timer.stop(TIMER_NAME);
        return awsProxyResponse;
    }
}
