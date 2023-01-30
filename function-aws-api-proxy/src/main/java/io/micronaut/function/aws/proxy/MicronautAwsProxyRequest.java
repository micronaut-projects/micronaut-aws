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

import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.serverless.proxy.internal.jaxrs.AwsProxySecurityContext;
import com.amazonaws.serverless.proxy.model.ApiGatewayRequestIdentity;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.serverless.proxy.model.Headers;
import com.amazonaws.serverless.proxy.model.MultiValuedTreeMap;
import com.amazonaws.serverless.proxy.model.SingleValueHeaders;
import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
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
import io.micronaut.http.MediaType;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;

import javax.ws.rs.core.SecurityContext;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.amazonaws.serverless.proxy.RequestReader.ALB_CONTEXT_PROPERTY;
import static com.amazonaws.serverless.proxy.RequestReader.API_GATEWAY_CONTEXT_PROPERTY;
import static com.amazonaws.serverless.proxy.RequestReader.API_GATEWAY_EVENT_PROPERTY;
import static com.amazonaws.serverless.proxy.RequestReader.API_GATEWAY_STAGE_VARS_PROPERTY;
import static com.amazonaws.serverless.proxy.RequestReader.JAX_SECURITY_CONTEXT_PROPERTY;
import static com.amazonaws.serverless.proxy.RequestReader.LAMBDA_CONTEXT_PROPERTY;

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

        this.headers = awsProxyRequest.getMultiValueHeaders() != null || awsProxyRequest.getHeaders() != null ?
            new AwsHeaders(awsProxyRequest.getMultiValueHeaders(), awsProxyRequest.getHeaders()) : new SimpleHttpHeaders(ConversionService.SHARED);

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
        if (isSecurityContextPresent (securityContext)) {
            setAttribute("micronaut.AUTHENTICATION", securityContext.getUserPrincipal());
        }
    }

    /**
     *
     * @param securityContext Security Context
     * @return returns false if the security context is not present, the associated event is null or the event's request context is null
     */
    static boolean isSecurityContextPresent(@Nullable SecurityContext securityContext) {
        if (securityContext == null) {
            return false;
        }
        if (securityContext instanceof AwsProxySecurityContext) {
            AwsProxySecurityContext awsProxySecurityContext = (AwsProxySecurityContext) securityContext;
            if (awsProxySecurityContext.getEvent() == null ||
                    awsProxySecurityContext.getEvent().getRequestContext() == null ||
                    awsProxySecurityContext.getEvent().getRequestContext().getIdentity() == null) {
                           return false;
            }
        }
        return true;
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
    @NonNull
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
    @NonNull
    public HttpParameters getParameters() {
        return parameters;
    }

    @Override
    @NonNull
    public HttpMethod getMethod() {
        return httpMethod;
    }

    @Override
    @NonNull
    public URI getUri() {
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            // this is not a critical failure, we just put a static region in the URI
            region = "us-east-1";
        }

        final Headers multiValueHeaders = awsProxyRequest.getMultiValueHeaders();
        final SingleValueHeaders singleValueHeaders = awsProxyRequest.getHeaders();


        String hostHeader = multiValueHeaders != null ? multiValueHeaders.getFirst(HttpHeaders.HOST) : null;

        hostHeader = hostHeader == null && singleValueHeaders != null ? singleValueHeaders.get(HttpHeaders.HOST) : null;

        final AwsProxyRequestContext requestContext = awsProxyRequest.getRequestContext();

        if (requestContext != null && !isValidHost(hostHeader, requestContext.getApiId(), region)) {
            hostHeader = requestContext.getApiId() +
                    ".execute-api." +
                    region +
                    ".amazonaws.com";
        }

        return URI.create(getScheme() + "://" + hostHeader + path);
    }

    @NonNull
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

    @NonNull
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
    @NonNull
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    @NonNull
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNull
    public Optional<T> getBody() {
        if (decodedBody != null) {
            return Optional.of(decodedBody);
        }
        final String body = awsProxyRequest.getBody();
        if (awsProxyRequest.isBase64Encoded() && body != null) {
            return (Optional<T>) Optional.ofNullable(Base64.getMimeDecoder().decode(body));
        }
        return (Optional<T>) Optional.ofNullable(body);
    }

    @Override
    @NonNull
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

    /**
     * @return true if body was already decoded, false otherwise
     */
    public boolean isBodyDecoded() {
        return decodedBody != null;
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
                    return strings;
                }
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                return params.getFirst(name.toString());
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
    }

    /**
     * Implementation of {@link HttpHeaders} for AWS.
     */
    private static class AwsHeaders implements HttpHeaders {

        private final Map<String, List<String>> headers;

        public AwsHeaders(@Nullable Headers multivalueHeaders, @Nullable SingleValueHeaders singleValueHeaders) {
            if (multivalueHeaders == null && singleValueHeaders == null) {
                headers = Collections.emptyMap();
            } else {
                headers = new HashMap<>();
                if (CollectionUtils.isNotEmpty(multivalueHeaders)) {
                    for (String name : multivalueHeaders.keySet()) {
                        String headerName = normalizeHttpHeaderCase(name);
                        headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                        headers.get(headerName).addAll(multivalueHeaders.get(headerName));
                    }
                }
                if (CollectionUtils.isNotEmpty(singleValueHeaders)) {
                    for (String name : singleValueHeaders.keySet()) {
                        String headerName = normalizeHttpHeaderCase(name);
                        headers.computeIfAbsent(headerName, s -> new ArrayList<>());
                        headers.get(headerName).add(singleValueHeaders.get(headerName));
                    }
                }
            }
        }

        @Override
        public List<String> getAll(CharSequence name) {
            if (!headers.containsKey(name)) {
                return Collections.emptyList();
            }
            List<String> values = headers.get(name);
            if (values == null) {
                return Collections.emptyList();
            }
            return values;
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            List<String> values = getAll(name);
            if (CollectionUtils.isEmpty(values)) {
                return null;
            }
            return values.get(0);
        }

        @Override
        public Set<String> names() {
            return headers.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return headers.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            final String v = get(name);
            if (v != null) {
                return ConversionService.SHARED.convert(v, conversionContext);
            }
            return Optional.empty();
        }


        @NonNull
        private static String normalizeHttpHeaderCase(@NonNull String headerName) {
            if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT)) {
                return HttpHeaders.ACCEPT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CH)) {
                return HttpHeaders.ACCEPT_CH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CH_LIFETIME)) {
                return HttpHeaders.ACCEPT_CH_LIFETIME;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CHARSET)) {
                return HttpHeaders.ACCEPT_CHARSET;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) {
                return HttpHeaders.ACCEPT_ENCODING;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_LANGUAGE)) {
                return HttpHeaders.ACCEPT_LANGUAGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_RANGES)) {
                return HttpHeaders.ACCEPT_RANGES;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_PATCH)) {
                return HttpHeaders.ACCEPT_PATCH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)) {
                return HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)) {
                return HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)) {
                return HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
                return HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)) {
                return HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_MAX_AGE)) {
                return HttpHeaders.ACCESS_CONTROL_MAX_AGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)) {
                return HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)) {
                return HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.AGE)) {
                return HttpHeaders.AGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ALLOW)) {
                return HttpHeaders.ALLOW;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                return HttpHeaders.AUTHORIZATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION_INFO)) {
                return HttpHeaders.AUTHORIZATION_INFO;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CACHE_CONTROL)) {
                return HttpHeaders.CACHE_CONTROL;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONNECTION)) {
                return HttpHeaders.CONNECTION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_BASE)) {
                return HttpHeaders.CONTENT_BASE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
                return HttpHeaders.CONTENT_DISPOSITION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_DPR)) {
                return HttpHeaders.CONTENT_DPR;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING)) {
                return HttpHeaders.CONTENT_ENCODING;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LANGUAGE)) {
                return HttpHeaders.CONTENT_LANGUAGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                return HttpHeaders.CONTENT_LENGTH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LOCATION)) {
                return HttpHeaders.CONTENT_LOCATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TRANSFER_ENCODING)) {
                return HttpHeaders.CONTENT_TRANSFER_ENCODING;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_MD5)) {
                return HttpHeaders.CONTENT_MD5;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_RANGE)) {
                return HttpHeaders.CONTENT_RANGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                return HttpHeaders.CONTENT_TYPE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.COOKIE)) {
                return HttpHeaders.COOKIE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY)) {
                return HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.DATE)) {
                return HttpHeaders.DATE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.DEVICE_MEMORY)) {
                return HttpHeaders.DEVICE_MEMORY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.DOWNLINK)) {
                return HttpHeaders.DOWNLINK;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.DPR)) {
                return HttpHeaders.DPR;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ECT)) {
                return HttpHeaders.ECT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ETAG)) {
                return HttpHeaders.ETAG;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.EXPECT)) {
                return HttpHeaders.EXPECT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.EXPIRES)) {
                return HttpHeaders.EXPIRES;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.FEATURE_POLICY)) {
                return HttpHeaders.FEATURE_POLICY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.FORWARDED)) {
                return HttpHeaders.FORWARDED;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.FROM)) {
                return HttpHeaders.FROM;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                return HttpHeaders.HOST;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_MATCH)) {
                return HttpHeaders.IF_MATCH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_MODIFIED_SINCE)) {
                return HttpHeaders.IF_MODIFIED_SINCE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_NONE_MATCH)) {
                return HttpHeaders.IF_NONE_MATCH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_RANGE)) {
                return HttpHeaders.IF_RANGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_UNMODIFIED_SINCE)) {
                return HttpHeaders.IF_UNMODIFIED_SINCE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.LAST_MODIFIED)) {
                return HttpHeaders.LAST_MODIFIED;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.LINK)) {
                return HttpHeaders.LINK;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
                return HttpHeaders.LOCATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.MAX_FORWARDS)) {
                return HttpHeaders.MAX_FORWARDS;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.ORIGIN)) {
                return HttpHeaders.ORIGIN;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.PRAGMA)) {
                return HttpHeaders.PRAGMA;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.PROXY_AUTHENTICATE)) {
                return HttpHeaders.PROXY_AUTHENTICATE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.PROXY_AUTHORIZATION)) {
                return HttpHeaders.PROXY_AUTHORIZATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.RANGE)) {
                return HttpHeaders.RANGE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.REFERER)) {
                return HttpHeaders.REFERER;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.REFERRER_POLICY)) {
                return HttpHeaders.REFERRER_POLICY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.RETRY_AFTER)) {
                return HttpHeaders.RETRY_AFTER;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.RTT)) {
                return HttpHeaders.RTT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SAVE_DATA)) {
                return HttpHeaders.SAVE_DATA;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY1)) {
                return HttpHeaders.SEC_WEBSOCKET_KEY1;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY2)) {
                return HttpHeaders.SEC_WEBSOCKET_KEY2;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_LOCATION)) {
                return HttpHeaders.SEC_WEBSOCKET_LOCATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_ORIGIN)) {
                return HttpHeaders.SEC_WEBSOCKET_ORIGIN;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_PROTOCOL)) {
                return HttpHeaders.SEC_WEBSOCKET_PROTOCOL;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_VERSION)) {
                return HttpHeaders.SEC_WEBSOCKET_VERSION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY)) {
                return HttpHeaders.SEC_WEBSOCKET_KEY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_ACCEPT)) {
                return HttpHeaders.SEC_WEBSOCKET_ACCEPT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SERVER)) {
                return HttpHeaders.SERVER;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                return HttpHeaders.SET_COOKIE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SET_COOKIE2)) {
                return HttpHeaders.SET_COOKIE2;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.SOURCE_MAP)) {
                return HttpHeaders.SOURCE_MAP;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.TE)) {
                return HttpHeaders.TE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.TRAILER)) {
                return HttpHeaders.TRAILER;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                return HttpHeaders.TRANSFER_ENCODING;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.UPGRADE)) {
                return HttpHeaders.UPGRADE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.USER_AGENT)) {
                return HttpHeaders.USER_AGENT;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.VARY)) {
                return HttpHeaders.VARY;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.VIA)) {
                return HttpHeaders.VIA;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.VIEWPORT_WIDTH)) {
                return HttpHeaders.VIEWPORT_WIDTH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WARNING)) {
                return HttpHeaders.WARNING;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_LOCATION)) {
                return HttpHeaders.WEBSOCKET_LOCATION;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_ORIGIN)) {
                return HttpHeaders.WEBSOCKET_ORIGIN;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_PROTOCOL)) {
                return HttpHeaders.WEBSOCKET_PROTOCOL;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WIDTH)) {
                return HttpHeaders.WIDTH;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.WWW_AUTHENTICATE)) {
                return HttpHeaders.WWW_AUTHENTICATE;
            } else if (headerName.equalsIgnoreCase(HttpHeaders.X_AUTH_TOKEN)) {
                return HttpHeaders.X_AUTH_TOKEN;
            }
            return headerName;
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
