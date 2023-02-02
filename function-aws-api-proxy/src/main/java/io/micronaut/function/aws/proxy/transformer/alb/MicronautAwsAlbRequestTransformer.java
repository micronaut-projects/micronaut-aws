package io.micronaut.function.aws.proxy.transformer.alb;

import static java.util.function.Predicate.not;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.DefaultMicronautAwsRequestBodySupplier;
import io.micronaut.function.aws.proxy.LambdaUtils;
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
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.http.simple.cookies.SimpleCookies;
import jakarta.inject.Singleton;

@Singleton
public class MicronautAwsAlbRequestTransformer<T>
    implements MicronautAwsRequestTransformer<ApplicationLoadBalancerRequestEvent, T> {
    private final ApplicationContext context;
    private MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private static final String CF_PROTOCOL_HEADER_NAME = "CloudFront-Forwarded-Proto";
    private static final String PROTOCOL_HEADER_NAME = "X-Forwarded-Proto";

    public MicronautAwsAlbRequestTransformer(final ApplicationContext context) {
        this.context = context;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public MicronautAwsRequest<T> toMicronautRequest(final ApplicationLoadBalancerRequestEvent source) {
        SimpleHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED);

        source.getHeaders().entrySet().stream()
            .filter(not(h -> Objects.equals(HttpHeaders.COOKIE, h.getKey())))
            .forEach(entry -> {
                List<String> headerValues = Arrays.asList(entry.getValue().split(","));
                headerValues.forEach(hv -> headers.add(entry.getKey(), hv));
            });

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

        MicronautAwsRequestBodySupplier<?> bodySupplier = new DefaultMicronautAwsRequestBodySupplier<>(mediaTypeCodec, body);

        String uri = source.getPath() + Optional.ofNullable(source.getQueryStringParameters())
            .map(queryParams -> queryParams.entrySet().stream()
                .map(entry -> Arrays.stream(entry.getValue().split(","))
                    .map(queryValue -> entry.getKey() + "=" + queryValue)
                    .collect(Collectors.joining("&"))
                ))
            .map(query -> "?" + query);

        MicronautAwsRequest<T> request = MicronautAwsRequest.<T>builder()
            .headers(headers)
            .bodySupplier(bodySupplier)
            .method(HttpMethod.parse(source.getHttpMethod()))
            .uri(getUri(headers, uri))
            .build();

        return request;
    }

    private URI getUri(HttpHeaders headers, String pathQuery) {
        String hostHeader = headers.get(HttpHeaders.HOST);

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
}
