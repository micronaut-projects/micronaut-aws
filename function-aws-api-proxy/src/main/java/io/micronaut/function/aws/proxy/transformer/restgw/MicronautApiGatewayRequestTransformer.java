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
package io.micronaut.function.aws.proxy.transformer.restgw;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.DefaultMicronautAwsRequestBodySupplier;
import io.micronaut.function.aws.proxy.MicronautAwsRequest;
import io.micronaut.function.aws.proxy.MicronautAwsRequestBodySupplier;
import io.micronaut.function.aws.proxy.MicronautAwsRequestTransformer;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.CookieFactory;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.cookies.SimpleCookies;
import jakarta.inject.Singleton;

@Singleton
public class MicronautApiGatewayRequestTransformer<T>
    implements MicronautAwsRequestTransformer<APIGatewayProxyRequestEvent, T> {
    private final ApplicationContext context;
    private MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private static final String CF_PROTOCOL_HEADER_NAME = "CloudFront-Forwarded-Proto";
    private static final String PROTOCOL_HEADER_NAME = "X-Forwarded-Proto";

    public MicronautApiGatewayRequestTransformer(final ApplicationContext context) {
        this.context = context;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public MicronautAwsRequest<T> toMicronautRequest(final APIGatewayProxyRequestEvent source) {
        SimpleHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED);
        source.getHeaders()
            .forEach(headers::add);

        source.getMultiValueHeaders()
            .forEach((k, mv) -> mv.forEach(v -> headers.add(k, v)));

        String cookieHeaderValue = headers.get(HttpHeaders.COOKIE);
        headers.remove(HttpHeaders.COOKIE);
        SimpleCookies cookies = new SimpleCookies(ConversionService.SHARED);

        if (StringUtils.isNotEmpty(cookieHeaderValue)) {
            CookieFactory cookieFactory = context.getBean(CookieFactory.class);
            // https://www.rfc-editor.org/rfc/rfc6265#section-4.2.1
            Arrays.stream(cookieHeaderValue.split("; "))
                .forEach(cookieValue -> {
                    String[] cookiePair = cookieValue.split("=");
                    Cookie cookie = cookieFactory.create(cookiePair[0].trim(), cookiePair[1].trim());
                    cookies.put(cookie.getName(), cookie);
                });
        }

        String body = getBody(source);
        MediaType mediaType = headers.getContentType().map(MediaType::of).orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec mediaTypeCodec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec " + mediaType + " not found"));

        MicronautAwsRequestBodySupplier<T> bodySupplier = new DefaultMicronautAwsRequestBodySupplier<>(mediaTypeCodec, body);

        String queryString = Optional.ofNullable(source.getMultiValueQueryStringParameters())
            .map(queryStringParams -> queryStringParams.entrySet().stream()
                .flatMap(queryMap -> queryMap.getValue().stream()
                    .map(queryValue -> queryMap.getKey() + "=" + queryValue))
                .collect(Collectors.joining("&")))
            .orElse(null);

        String uri =
            source.getPath() + Optional.ofNullable(queryString)
                .filter(StringUtils::isNotEmpty)
                .map(query -> "?" + query)
                .orElse(StringUtils.EMPTY_STRING);

        MicronautAwsRequest<T> request = MicronautAwsRequest.<T>builder()
            .headers(headers)
            .bodySupplier(bodySupplier)
            .method(HttpMethod.parse(source.getHttpMethod()))
            .uri(getUri(headers, uri, source.getRequestContext().getApiId()))
            .build();

        return request;
    }

    private String getBody(final APIGatewayProxyRequestEvent source) {
        if (Objects.equals(Boolean.TRUE, source.getIsBase64Encoded())) {
            byte[] decodedBytes = Base64.getDecoder().decode(source.getBody());
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

        return source.getBody();
    }

    private URI getUri(HttpHeaders headers, String path, String apiId) {
        String region = System.getenv("AWS_REGION");
        if (region == null) {
            // this is not a critical failure, we just put a static region in the URI
            region = "us-east-1";
        }

        String hostHeader = headers.get(HttpHeaders.HOST);

        if (StringUtils.isNotEmpty(hostHeader) && !isValidHost(hostHeader, apiId, region)) {
            hostHeader = apiId +
                ".execute-api." +
                region +
                ".amazonaws.com";
        }

        return URI.create(getScheme(headers) + "://" + hostHeader + path);

    }

    private String getScheme(HttpHeaders headers) {
        // if we don't have any headers to deduce the value we assume HTTPS - API Gateway's default
        if (headers.isEmpty()) {
            return "https";
        }
        String cfScheme = headers.get(CF_PROTOCOL_HEADER_NAME);
        if (cfScheme != null && SecurityUtils.isValidScheme(cfScheme)) {
            return cfScheme;
        }
        String gwScheme = headers.get(PROTOCOL_HEADER_NAME);
        if (gwScheme != null && SecurityUtils.isValidScheme(gwScheme)) {
            return gwScheme;
        }
        // https is our default scheme
        return "https";
    }

    private boolean isValidHost(String host, String apiId, String region) {
        return Optional.ofNullable(host)
            .filter(StringUtils::isNotEmpty)
            .map(h -> h.equals(apiId + ".execute-api." + region + ".amazonaws.com"))
            .orElse(false);
    }
}
