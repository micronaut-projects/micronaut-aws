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
package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.FullHttpRequest;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.MutableServletHttpRequest;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ParsedBodyHolder;
import io.micronaut.servlet.http.ByteArrayByteBuffer;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Base class for all the versions of the Gateway events.
 *
 * @param <T> The body type
 * @param <REQ> The request event type
 * @param <RES> The response event type
 */
@Internal
@SuppressWarnings("java:S119") // More descriptive generics are better here
public abstract class ApiGatewayServletRequest<T, REQ, RES> implements MutableServletHttpRequest<REQ, T>, ServletExchange<REQ, RES>, FullHttpRequest<T>, ParsedBodyHolder<T> {

    private static final Set<Class<?>> RAW_BODY_TYPES = CollectionUtils.setOf(String.class, byte[].class, ByteBuffer.class, InputStream.class);

    protected ConversionService conversionService;
    protected final REQ requestEvent;
    private URI uri;
    private final HttpMethod httpMethod;
    private final Logger log;
    private Cookies cookies;
    private MutableConvertibleValues<Object> attributes;
    private Supplier<Optional<T>> body;
    private T parsedBody;
    private T overriddenBody;

    private ByteArrayByteBuffer<T> servletByteBuffer;

    protected ApiGatewayServletRequest(
        ConversionService conversionService,
        REQ request,
        URI uri,
        HttpMethod httpMethod,
        Logger log,
        BodyBuilder bodyBuilder
    ) {
        this.conversionService = conversionService;
        this.requestEvent = request;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.log = log;
        this.body = SupplierUtil.memoizedNonEmpty(() -> {
            T built = parsedBody != null ? parsedBody :  (T) bodyBuilder.buildBody(this::getInputStream, this);
            return Optional.ofNullable(built);
        });
    }

    public abstract byte[] getBodyBytes() throws IOException;

    protected static HttpMethod parseMethod(Supplier<String> httpMethodConsumer) {
        try {
            return HttpMethod.valueOf(httpMethodConsumer.get());
        } catch (IllegalArgumentException e) {
            return HttpMethod.CUSTOM;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (servletByteBuffer == null) {
            // Read all the input stream into memory, so we can parse it multiple times (where we need different formats)
            this.servletByteBuffer = new ByteArrayByteBuffer<>(getBodyBytes());
        }
        if (parsedBody != null) {
            // We have already parsed a body, this is a second call to getInputStream probably for a different type, so reset the reader.
            this.servletByteBuffer.readerIndex(0);
        }
        return servletByteBuffer.toInputStream();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ServletHttpRequest<REQ, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public REQ getNativeRequest() {
        return requestEvent;
    }

    @Override
    public HttpMethod getMethod() {
        return httpMethod;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @NonNull
    @Override
    public Cookies getCookies() {
        Cookies localCookies = this.cookies;
        if (localCookies == null) {
            synchronized (this) { // double check
                localCookies = this.cookies;
                if (localCookies == null) {
                    localCookies = new AwsCookies(getPath(), getHeaders(), conversionService);
                    this.cookies = localCookies;
                }
            }
        }
        return localCookies;
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> localAttributes = this.attributes;
        if (localAttributes == null) {
            synchronized (this) { // double check
                localAttributes = this.attributes;
                if (localAttributes == null) {
                    localAttributes = new MutableConvertibleValuesMap<>();
                    this.attributes = localAttributes;
                }
            }
        }
        return localAttributes;
    }

    @NonNull
    @Override
    public Optional<T> getBody() {
        if (overriddenBody != null) {
            return Optional.of(overriddenBody);
        }
        return this.body.get();
    }

    @NonNull
    @Override
    public <B> Optional<B> getBody(Argument<B> arg) {
        return getBody().map(t -> conversionService.convertRequired(t, arg));
    }

    /**
     *
     * @param contentType Content Type
     * @return returns true if the content type is either application/x-www-form-urlencoded or multipart/form-data
     */
    protected boolean isFormSubmission(MediaType contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }

    @Override
    public MutableHttpRequest<T> cookie(Cookie cookie) {
        return this;
    }

    @Override
    public MutableHttpRequest<T> uri(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <B> MutableHttpRequest<B> body(B body) {
        this.overriddenBody = (T) body;
        return (MutableHttpRequest<B>) this;
    }

    /**
     * Parse the parameters from the body.
     * @param queryStringParameters Any query string parameters
     * @return The parameters
     */
    protected MapListOfStringAndMapStringMutableHttpParameters getParametersFromBody(Map<String, String> queryStringParameters) {
        Map<String, List<String>> parameters = null;
        try {
            parameters = new QueryStringDecoder(new String(getBodyBytes(), getCharacterEncoding()), false).parameters();
        } catch (IOException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Error decoding form data: " + ex.getMessage(), ex);
            }
        }
        return new MapListOfStringAndMapStringMutableHttpParameters(conversionService, parameters, queryStringParameters);
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void setParsedBody(T body) {
        this.parsedBody = body;
    }

    @Override
    public @Nullable ByteBuffer<?> contents() {
        try {
            if (servletByteBuffer == null) {
                this.servletByteBuffer = new ByteArrayByteBuffer<>(getInputStream().readAllBytes());
            }
            return servletByteBuffer;
        } catch (IOException e) {
            throw new IllegalStateException("Error getting all body contents", e);
        }
    }

    @Override
    public @Nullable ExecutionFlow<ByteBuffer<?>> bufferContents() {
        ByteBuffer<?> contents = contents();
        if (contents == null) {
            return null;
        }
        return ExecutionFlow.just(contents);
    }

    /**
     *
     * @param bodySupplier HTTP Request's Body Supplier
     * @param base64EncodedSupplier Whether the body is Base 64 encoded
     * @return body bytes
     * @throws IOException if the body is empty
     */
    protected byte[] getBodyBytes(@NonNull Supplier<String> bodySupplier, @NonNull BooleanSupplier base64EncodedSupplier) throws IOException {
        String requestBody = bodySupplier.get();
        if (StringUtils.isEmpty(requestBody)) {
            throw new IOException("Empty Body");
        }
        return base64EncodedSupplier.getAsBoolean() ?
            Base64.getDecoder().decode(requestBody) : requestBody.getBytes(getCharacterEncoding());
    }

    @NonNull
    protected static List<String> splitCommaSeparatedValue(@Nullable String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    @NonNull
    protected static Map<String, List<String>> transformCommaSeparatedValue(@Nullable Map<String, String> input) {
        if (input == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> output = new HashMap<>();
        for (var entry: input.entrySet()) {
            output.put(entry.getKey(), splitCommaSeparatedValue(entry.getValue()));
        }
        return output;
    }

    /**
     *
     * @param queryStringParametersSupplier Query String parameters as a map with key string and value string
     * @param multiQueryStringParametersSupplier Query String parameters as a map with key string and value list of strings
     * @return Mutable HTTP parameters
     */
    @NonNull
    protected MutableHttpParameters getParameters(@NonNull Supplier<Map<String, String>> queryStringParametersSupplier,
                                                  @NonNull Supplier<Map<String, List<String>>> multiQueryStringParametersSupplier) {
        Map<String, List<String>> multi = multiQueryStringParametersSupplier.get();
        Map<String, String> single = queryStringParametersSupplier.get();
        MediaType mediaType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        if (isFormSubmission(mediaType)) {
            return getParametersFromBody(MapCollapseUtils.collapse(MapCollapseUtils.collapse(multi, single)));
        } else {
            return new MapListOfStringAndMapStringMutableHttpParameters(conversionService, multi, single);
        }
    }

    /**
     *
     * @param singleHeaders Single value headers
     * @param multiValueHeaders Multi-value headers
     * @return The combined mutable headers
     */
    @NonNull
    protected MutableHttpHeaders getHeaders(@NonNull Supplier<Map<String, String>> singleHeaders, @NonNull Supplier<Map<String, List<String>>> multiValueHeaders) {
        return new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(multiValueHeaders.get(), singleHeaders.get()), conversionService);
    }
}
