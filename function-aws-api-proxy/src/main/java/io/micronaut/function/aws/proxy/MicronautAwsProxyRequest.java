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

import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.serverless.proxy.model.Headers;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.http.simple.cookies.SimpleCookies;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.SecurityContext;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.serverless.proxy.RequestReader.*;

/**
 * Implementation of {@link HttpRequest} that backs onto a {@link AwsProxyRequest} object.
 *
 * @author graemerocher
 * @since 1.1
 * @param <T> The body type
 */
public class MicronautAwsProxyRequest<T> implements HttpRequest<T> {
    private static final String CF_PROTOCOL_HEADER_NAME = "CloudFront-Forwarded-Proto";
    private static final String PROTOCOL_HEADER_NAME = "X-Forwarded-Proto";

    private final AwsProxyRequest awsProxyRequest;
    private final HttpMethod httpMethod;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final Cookies cookies = new SimpleCookies(ConversionService.SHARED);
    private final HttpHeaders headers;
    private final HttpParameters parameters;
    private final String path;
    private MicronautAwsProxyResponse<?> response;
    private T decodedBody;

    /**
     * Default constructor.
     *
     * @param path The path
     * @param awsProxyRequest The underlying request
     * @param securityContext The {@link SecurityContext}
     * @param lambdaContext The lambda context
     * @param config The container configuration
     */
    MicronautAwsProxyRequest(
            String path,
            AwsProxyRequest awsProxyRequest,
            SecurityContext securityContext,
            Context lambdaContext,
            ContainerConfig config) {
        this.awsProxyRequest = awsProxyRequest;
        this.path = path;
        this.httpMethod = HttpMethod.valueOf(awsProxyRequest.getHttpMethod());
        final Headers multiValueHeaders = awsProxyRequest.getMultiValueHeaders();
        this.headers = multiValueHeaders != null ? new AwsHeaders() : new SimpleHttpHeaders(ConversionService.SHARED);
        final MultiValuedTreeMap<String, String> params = awsProxyRequest.getMultiValueQueryStringParameters();
        this.parameters = params != null ? new AwsParameters() : new SimpleHttpParameters(ConversionService.SHARED);

        setAttribute(API_GATEWAY_CONTEXT_PROPERTY, awsProxyRequest.getRequestContext());
        setAttribute(API_GATEWAY_STAGE_VARS_PROPERTY, awsProxyRequest.getStageVariables());
        setAttribute(API_GATEWAY_EVENT_PROPERTY, awsProxyRequest);
        setAttribute(ALB_CONTEXT_PROPERTY, awsProxyRequest.getRequestContext().getElb());
        setAttribute(LAMBDA_CONTEXT_PROPERTY, lambdaContext);
        setAttribute(JAX_SECURITY_CONTEXT_PROPERTY, config);
        if (securityContext != null) {
            setAttribute("micronaut.AUTHENTICATION", securityContext.getUserPrincipal());
        }
    }

    /**
     * The backing {@link AwsProxyRequest} object.
     * @return The backing {@link AwsProxyRequest} object.
     */
    public final AwsProxyRequest getAwsProxyRequest() {
        return awsProxyRequest;
    }

    /**
     * @return The response object
     */
    @Internal
    public MicronautAwsProxyResponse<?> getResponse() {
        if (response == null) {
            throw new IllegalStateException("Response not set");
        }
        return response;
    }

    /**
     * Sets the associated response object.
     * @param response The response
     */
    @Internal
    void setResponse(MicronautAwsProxyResponse<?> response) {
        this.response = response;
    }

    @Override
    @Nonnull
    public Cookies getCookies() {
        return cookies;
    }

    @Override
    @Nonnull
    public HttpParameters getParameters() {
        return parameters;
    }

    @Override
    @Nonnull
    public HttpMethod getMethod() {
        return httpMethod;
    }

    @Override
    @Nonnull
    public URI getUri() {
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            // this is not a critical failure, we just put a static region in the URI
            region = "us-east-1";
        }

        String hostHeader = awsProxyRequest.getMultiValueHeaders().getFirst(HttpHeaders.HOST);
        if (!SecurityUtils.isValidHost(hostHeader, awsProxyRequest.getRequestContext().getApiId(), region)) {
            hostHeader = new StringBuilder().append(awsProxyRequest.getRequestContext().getApiId())
                    .append(".execute-api.")
                    .append(region)
                    .append(".amazonaws.com").toString();
        }

        return URI.create(getScheme() + "://" + hostHeader + path);
    }

    @Override
    @Nonnull
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    @Nonnull
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    public Optional<T> getBody() {
        if (decodedBody != null) {
            return Optional.of(decodedBody);
        }
        final String body = awsProxyRequest.getBody();
        return (Optional<T>) Optional.ofNullable(body);
    }

    @Override
    @Nonnull
    public <T1> Optional<T1> getBody(Argument<T1> type) {
        if (decodedBody != null) {
            return ConversionService.SHARED.convert(decodedBody, type);
        }
        final String body = awsProxyRequest.getBody();
        if (body != null) {
            if (type.getType().isInstance(body)) {
                return (Optional<T1>) Optional.of(body);
            } else {
                final byte[] bytes = body.getBytes(getCharacterEncoding());
                return ConversionService.SHARED.convert(bytes, type);
            }
        }
        return Optional.empty();
    }

    /**
     * The decoded body.
     * @param decodedBody The body
     */
    @Internal
    void setDecodedBody(T decodedBody) {
        this.decodedBody = decodedBody;
    }

    private String getScheme() {
        // if we don't have any headers to deduce the value we assume HTTPS - API Gateway's default
        if (awsProxyRequest.getMultiValueHeaders() == null) {
            return "https";
        }
        String cfScheme = awsProxyRequest.getMultiValueHeaders().getFirst(CF_PROTOCOL_HEADER_NAME);
        if (cfScheme != null && SecurityUtils.isValidScheme(cfScheme)) {
            return cfScheme;
        }
        String gwScheme = awsProxyRequest.getMultiValueHeaders().getFirst(PROTOCOL_HEADER_NAME);
        if (gwScheme != null && SecurityUtils.isValidScheme(gwScheme)) {
            return gwScheme;
        }
        // https is our default scheme
        return "https";
    }

    /**
     * Implementation of {@link HttpParameters} for AWS.
     *
     * @author graemerocher
     * @since 1.1
     */
    private class AwsParameters implements HttpParameters {

        private MultiValuedTreeMap<String, String> params = awsProxyRequest.getMultiValueQueryStringParameters();

        @Override
        public List<String> getAll(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                final List<String> strings = params.get(name.toString());
                if (CollectionUtils.isNotEmpty(strings)) {
                    return strings.stream().map(v -> decodeValue(name, name.toString())).collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                final String v = params.getFirst(name.toString());
                if (v != null) {
                    return decodeValue(name, v);
                }
                return v;
            }
            return null;
        }

        @Override
        public Set<String> names() {
            return params.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return params.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            final String v = get(name);
            if (v != null) {
                return ConversionService.SHARED.convert(v, conversionContext);
            }
            return Optional.empty();
        }

        private String decodeValue(CharSequence name, String v) {
            try {
                return URLDecoder.decode(v, getCharacterEncoding().name());
            } catch (UnsupportedEncodingException e) {
                throw new CodecException("Error decoding parameter: " + name, e);
            }
        }
    }

    /**
     * Implementation of {@link HttpHeaders} for AWS.
     */
    private class AwsHeaders implements HttpHeaders {

        private Headers multiValueHeaders = awsProxyRequest.getMultiValueHeaders();

        @Override
        public List<String> getAll(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                return multiValueHeaders.get(name.toString());
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                return multiValueHeaders.getFirst(name.toString());
            }
            return null;
        }

        @Override
        public Set<String> names() {
            return multiValueHeaders.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return multiValueHeaders.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            final String v = get(name);
            if (v != null) {
                return ConversionService.SHARED.convert(v, conversionContext);
            }
            return Optional.empty();
        }
    }
}
