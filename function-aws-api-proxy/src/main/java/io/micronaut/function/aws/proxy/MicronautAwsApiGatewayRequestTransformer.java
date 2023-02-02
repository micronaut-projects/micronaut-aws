package io.micronaut.function.aws.proxy;

import static java.util.function.Predicate.not;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;
import jakarta.inject.Singleton;

@Singleton
@Introspected
public class MicronautAwsApiGatewayRequestTransformer<T>
    implements MicronautAwsRequestTransformer<APIGatewayProxyRequestEvent, T> {
    private final ApplicationContext context;
    private MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private static final String CF_PROTOCOL_HEADER_NAME = "CloudFront-Forwarded-Proto";
    private static final String PROTOCOL_HEADER_NAME = "X-Forwarded-Proto";

    public MicronautAwsApiGatewayRequestTransformer(final ApplicationContext context) {
        this.context = context;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public MicronautAwsRequest<T> toMicronautRequest(final APIGatewayProxyRequestEvent source) {
        SimpleHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED);
        source.getHeaders().entrySet().stream()
            .filter(not(h -> Objects.equals(HttpHeaders.COOKIE, h.getKey())))
            .forEach(entry -> headers.add(entry.getKey(), entry.getValue()));

        source.getMultiValueHeaders()
            .forEach((k, mv) -> mv.forEach(v -> headers.add(k, v)));

        String cookieHeaderValue = headers.get(HttpHeaders.COOKIE);
        SimpleCookies cookies = new SimpleCookies(ConversionService.SHARED);

        if (StringUtils.isNotEmpty(cookieHeaderValue)) {
            // https://www.rfc-editor.org/rfc/rfc6265#section-4.2.1
            Arrays.stream(cookieHeaderValue.split("; "))
                .forEach(cookieValue -> {
                    String[] cookiePair = cookieValue.split("=");
                    Cookie cookie = new SimpleCookie(cookiePair[0].trim(), cookiePair[1].trim());
                    cookies.put(cookie.getName(), cookie);
                });
        }

        String body = LambdaUtils.decodeBody(source.getBody(), source.getIsBase64Encoded());
        MediaType mediaType = headers.getContentType().map(MediaType::of).orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec mediaTypeCodec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec " + mediaType + " not found"));

        MicronautAwsRequestBodySupplier<T> bodySupplier = new DefaultMicronautAwsRequestBodySupplier<>(mediaTypeCodec, body);

        String queryString = source.getMultiValueQueryStringParameters().entrySet().stream()
            .flatMap(queryMap -> queryMap.getValue().stream()
                .map(queryValue -> queryMap.getKey() + "=" + queryValue))
            .collect(Collectors.joining("&"));

        String uri =
            source.getPath() + Optional.ofNullable(queryString)
                .filter(StringUtils::isNotEmpty)
                .map(query -> "?" + query);

        MicronautAwsRequest<T> request = MicronautAwsRequest.<T>builder()
            .headers(headers)
            .bodySupplier(bodySupplier)
            .method(HttpMethod.parse(source.getHttpMethod()))
            .uri(getUri(headers, uri, source.getRequestContext().getApiId()))
            .build();

        return request;
    }

    private URI getUri(HttpHeaders headers, String pathQuery, String apiId) {
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

        return URI.create(getScheme(headers) + "://" + hostHeader + pathQuery);

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
        if (host == null) {
            return false;
        }
        if (host.endsWith(".amazonaws.com")) {
            String defaultHost = new StringBuilder().append(apiId)
                .append(".execute-api.")
                .append(region)
                .append(".amazonaws.com").toString();
            return host.equals(defaultHost);
        } else {
            return LambdaContainerHandler.getContainerConfig().getCustomDomainNames().contains(host);
        }
    }
}
