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

import com.amazonaws.serverless.proxy.model.ApiGatewayRequestIdentity;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.amazonaws.serverless.proxy.model.Headers;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpMethod;
import org.apache.commons.io.IOUtils;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link ServletToAwsProxyRequestAdapter}.
 *
 * @author Sergio del Amo
 */
@Singleton
public class DefaultServletToAwsProxyRequestAdapter implements ServletToAwsProxyRequestAdapter {

    @NonNull
    @Override
    public AwsProxyRequest createAwsProxyRequest(@NonNull HttpServletRequest request) {
        AwsProxyRequest awsProxyRequest = new AwsProxyRequest();
        awsProxyRequest.setRequestContext(createRequestContext(request));
        awsProxyRequest.setHttpMethod(request.getMethod());
        awsProxyRequest.setPath(request.getRequestURI());
        awsProxyRequest.setMultiValueHeaders(createHeaders(request));
        awsProxyRequest.setMultiValueQueryStringParameters(createParams(request));
        boolean isBase64Encoded = encodeBodyAsBase64();
        awsProxyRequest.setIsBase64Encoded(isBase64Encoded);
        createBody(request, isBase64Encoded).ifPresent(awsProxyRequest::setBody);
        return awsProxyRequest;
    }

    /**
     *
     * @param request A Servlet Request
     * @return The request context
     */
    @NonNull
    protected AwsProxyRequestContext createRequestContext(@NonNull HttpServletRequest request) {
        AwsProxyRequestContext requestContext = new AwsProxyRequestContext();
        requestContext.setIdentity(new ApiGatewayRequestIdentity());
        requestContext.setHttpMethod(request.getMethod());
        requestContext.setRequestTimeEpoch(Instant.now().toEpochMilli());
        return requestContext;
    }

    /**
     *
     * @param request a Servlet request
     * @return a {@link Headers} with the HTTP Headers of the request
     */
    @NonNull
    protected Headers createHeaders(@NonNull HttpServletRequest request) {
        Headers requestHeaders = new Headers();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String s = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(s);
            while (headers.hasMoreElements()) {
                String v = headers.nextElement();
                requestHeaders.add(s, v);
            }
        }
        return requestHeaders;
    }

    /**
     *
     * @param request Servlet Request
     * @return a {@link MultiValuedTreeMap} with the Request parameters
     */
    @NonNull
    protected MultiValuedTreeMap<String, String> createParams(@NonNull HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        MultiValuedTreeMap<String, String> params = new MultiValuedTreeMap<>();
        parameterMap.forEach(params::addAll);
        return params;
    }

    /**
     *
     * @return Whether to encode the body as Base64.
     */
    protected boolean encodeBodyAsBase64() {
        return true;
    }

    /**
     *
     * @param request A Servlet request
     * @param isBase64Encoded wether the request is base64 encoded or not
     * @return An optional wrapping the request body as a base64 string or an empty optional
     */
    @NonNull
    protected Optional<String> createBody(@NonNull HttpServletRequest request, boolean isBase64Encoded) {
        HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
        if (HttpMethod.permitsRequestBody(httpMethod)) {
            try (InputStream requestBody = request.getInputStream()) {
                byte[] data = IOUtils.toByteArray(requestBody);
                if (isBase64Encoded) {
                    return Optional.of(Base64.getEncoder().encodeToString(data));
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return Optional.empty();
    }
}
