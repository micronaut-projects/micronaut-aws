/*
 * Copyright 2017-2020 original authors
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
import com.amazonaws.serverless.proxy.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.*;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.SecurityContext;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.serverless.proxy.RequestReader.*;

/**
 * Implementation of {@link HttpRequest} that backs onto a {@link AwsProxyRequest} object.
 *
 * @param <T> The body type
 * @author graemerocher
 * @since 1.1
 */
public class MicronautAwsProxyRequest<T> implements HttpRequest<T> {
    private static final String HEADER_KEY_VALUE_SEPARATOR = "=";
    private static final String CF_PROTOCOL_HEADER_NAME = "CloudFront-Forwarded-Proto";
    private static final String PROTOCOL_HEADER_NAME = "X-Forwarded-Proto";

    private final AwsProxyRequest awsProxyRequest;
    private final HttpMethod httpMethod;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final HttpHeaders headers;
    private final HttpParameters parameters;
    private final String path;
    private final ContainerConfig config;
    private Cookies cookies;
    private MicronautAwsProxyResponse<?> response;
    private T decodedBody;

    /**
     * Default constructor.
     *
     * @param path            The path
     * @param awsProxyRequest The underlying request
     * @param securityContext The {@link SecurityContext}
     * @param lambdaContext   The lambda context
     * @param config          The container configuration
     */
    MicronautAwsProxyRequest(
            String path,
            AwsProxyRequest awsProxyRequest,
            SecurityContext securityContext,
            Context lambdaContext,
            ContainerConfig config) {
        this.config = config;
        this.awsProxyRequest = awsProxyRequest;
        this.path = path;
        final String httpMethod = awsProxyRequest.getHttpMethod();
        this.httpMethod = StringUtils.isNotEmpty(httpMethod) ? HttpMethod.valueOf(httpMethod) : HttpMethod.GET;
        final Headers multiValueHeaders = awsProxyRequest.getMultiValueHeaders();
        this.headers = multiValueHeaders != null ? new AwsHeaders() : new SimpleHttpHeaders(ConversionService.SHARED);
        final MultiValuedTreeMap<String, String> params = awsProxyRequest.getMultiValueQueryStringParameters();
        this.parameters = params != null ? new AwsParameters() : new SimpleHttpParameters(ConversionService.SHARED);

        final AwsProxyRequestContext requestContext = awsProxyRequest.getRequestContext();
        setAttribute(API_GATEWAY_CONTEXT_PROPERTY, requestContext);
        setAttribute(API_GATEWAY_STAGE_VARS_PROPERTY, awsProxyRequest.getStageVariables());
        setAttribute(API_GATEWAY_EVENT_PROPERTY, awsProxyRequest);
        if (requestContext != null) {
            setAttribute(ALB_CONTEXT_PROPERTY, requestContext.getElb());
        }
        setAttribute(LAMBDA_CONTEXT_PROPERTY, lambdaContext);
        setAttribute(JAX_SECURITY_CONTEXT_PROPERTY, config);
        if (securityContext != null && requestContext != null) {
            setAttribute("micronaut.AUTHENTICATION", securityContext.getUserPrincipal());
        }
    }

    /**
     * The backing {@link AwsProxyRequest} object.
     *
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
     *
     * @param response The response
     */
    @Internal
    void setResponse(MicronautAwsProxyResponse<?> response) {
        this.response = response;
    }

    @Override
    @Nonnull
    public Cookies getCookies() {
        if (cookies == null) {
            SimpleCookies simpleCookies = new SimpleCookies(ConversionService.SHARED);
            getHeaders().getAll(HttpHeaders.COOKIE).forEach(cookieValue -> {
                List<HeaderValue> parsedHeaders = parseHeaderValue(cookieValue, ";", ",");


                parsedHeaders.stream()
                        .filter(e -> e.getKey() != null)
                        .map(e -> new SimpleCookie(SecurityUtils.crlf(e.getKey()), SecurityUtils.crlf(e.getValue())))
                        .forEach(simpleCookie ->
                                simpleCookies.put(simpleCookie.getName(), simpleCookie));
            });

            cookies = simpleCookies;
        }
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

        final Headers multiValueHeaders = awsProxyRequest.getMultiValueHeaders();
        String hostHeader = multiValueHeaders != null ? multiValueHeaders.getFirst(HttpHeaders.HOST) : null;
        final AwsProxyRequestContext requestContext = awsProxyRequest.getRequestContext();

        if (requestContext != null && !isValidHost(hostHeader, requestContext.getApiId(), region)) {
            hostHeader = requestContext.getApiId() +
                    ".execute-api." +
                    region +
                    ".amazonaws.com";
        }

        return URI.create(getScheme() + "://" + hostHeader + path);
    }

    @Nonnull
    @Override
    public InetSocketAddress getRemoteAddress() {
        AwsProxyRequestContext requestContext = this.awsProxyRequest.getRequestContext();
        if (requestContext != null) {
            ApiGatewayRequestIdentity identity = requestContext.getIdentity();
            if (identity != null) {
                final String sourceIp = identity.getSourceIp();
                return new InetSocketAddress(sourceIp, 0);
            }
        }
        return HttpRequest.super.getRemoteAddress();
    }

    private boolean isValidHost(String host, String apiId, String region) {
        if (host == null) {
            return false;
        }
        if (host.endsWith(".amazonaws.com")) {
            String defaultHost = apiId +
                    ".execute-api." +
                    region +
                    ".amazonaws.com";
            return host.equals(defaultHost);
        } else {
            return config.getCustomDomainNames().contains(host);
        }
    }

    @Nonnull
    @Override
    public Optional<MediaType> getContentType() {
        Optional<MediaType> specifiedType = HttpRequest.super.getContentType();
        if (specifiedType.isPresent()) {
            return specifiedType;
        } else {
            return Optional.of(MediaType.APPLICATION_JSON_TYPE);
        }
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
        if (awsProxyRequest.isBase64Encoded()) {
            return (Optional<T>) Optional.ofNullable(Base64.getMimeDecoder().decode(body));
        }
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
            if (awsProxyRequest.isBase64Encoded()) {
                byte[] bytes = Base64.getMimeDecoder().decode(body);
                if (type.getType().isInstance(bytes)) {
                    return (Optional<T1>) Optional.of(bytes);
                }
                return ConversionService.SHARED.convert(bytes, type);
            }
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
     * Generic method to parse an HTTP header value and split it into a list of key/values for all its components.
     * When the property in the header does not specify a key the key field in the output pair is null and only the value
     * is populated. For example, The header <code>Accept: application/json; application/xml</code> will contain two
     * key value pairs with key null and the value set to application/json and application/xml respectively.
     *
     * @param headerValue        The string value for the HTTP header
     * @param valueSeparator     The separator to be used for parsing header values
     * @param qualifierSeparator the qualifier separator
     * @return A list of SimpleMapEntry objects with all of the possible values for the header.
     */
    protected List<HeaderValue> parseHeaderValue(
            String headerValue, String valueSeparator, String qualifierSeparator) {
        // Accept: text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8
        // Accept-Language: fr-CH, fr;q=0.9, en;q=0.8, de;q=0.7, *;q=0.5
        // Cookie: name=value; name2=value2; name3=value3
        // X-Custom-Header: YQ==

        List<HeaderValue> values = new ArrayList<>();
        if (headerValue == null) {
            return values;
        }

        for (String v : headerValue.split(valueSeparator)) {
            String curValue = v;
            float curPreference = 1.0f;
            HeaderValue newValue = new HeaderValue();
            newValue.setRawValue(v);

            for (String q : curValue.split(qualifierSeparator)) {

                String[] kv = q.split(HEADER_KEY_VALUE_SEPARATOR, 2);
                String key = null;
                String val = null;
                // no separator, set the value only
                if (kv.length == 1) {
                    val = q.trim();
                }
                // we have a separator
                if (kv.length == 2) {
                    // if the length of the value is 0 we assume that we are looking at a
                    // base64 encoded value with padding so we just set the value. This is because
                    // we assume that empty values in a key/value pair will contain at least a white space
                    if (kv[1].length() == 0) {
                        val = q.trim();
                    }
                    // this was a base64 string with an additional = for padding, set the value only
                    if ("=".equals(kv[1].trim())) {
                        val = q.trim();
                    } else { // it's a proper key/value set both
                        key = kv[0].trim();
                        val = ("".equals(kv[1].trim()) ? null : kv[1].trim());
                    }
                }

                if (newValue.getValue() == null) {
                    newValue.setKey(key);
                    newValue.setValue(val);
                } else {
                    // special case for quality q=
                    if ("q".equals(key)) {
                        curPreference = Float.parseFloat(val);
                    } else {
                        newValue.addAttribute(key, val);
                    }
                }
            }
            newValue.setPriority(curPreference);
            values.add(newValue);
        }

        // sort list by preference
        values.sort((HeaderValue first, HeaderValue second) -> {
            if ((first.getPriority() - second.getPriority()) < .001f) {
                return 0;
            }
            if (first.getPriority() < second.getPriority()) {
                return 1;
            }
            return -1;
        });
        return values;
    }

    /**
     * The decoded body.
     *
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
                    return strings.stream().map(v -> decodeValue(name, v)).collect(Collectors.toList());
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
                final List<String> strings = multiValueHeaders.get(name.toString());
                if (CollectionUtils.isNotEmpty(strings)) {
                    return strings;
                }
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

    /**
     * Class that represents a header value.
     */
    private static class HeaderValue {
        private String key;
        private String value;
        private String rawValue;
        private float priority;
        private Map<String, String> attributes;

        public HeaderValue() {
            attributes = new HashMap<>();
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRawValue() {
            return rawValue;
        }

        public void setRawValue(String rawValue) {
            this.rawValue = rawValue;
        }

        public float getPriority() {
            return priority;
        }

        public void setPriority(float priority) {
            this.priority = priority;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public void addAttribute(String key, String value) {
            attributes.put(key, value);
        }

        public String getAttribute(String key) {
            return attributes.get(key);
        }
    }
}
