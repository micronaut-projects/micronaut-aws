package io.micronaut.function.aws.proxy.transformer.httpgw;

import static java.util.function.Predicate.not;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.function.aws.proxy.LambdaUtils;
import io.micronaut.function.aws.proxy.MicronautAwsProxyConfiguration;
import io.micronaut.function.aws.proxy.MicronautAwsResponseTransformer;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import jakarta.inject.Singleton;

@Singleton
public class MicronautApiGatewayV2ResponseTransformer implements MicronautAwsResponseTransformer<APIGatewayV2HTTPResponse> {
    private final ApplicationContext context;
    private final boolean encodeBase64;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public MicronautApiGatewayV2ResponseTransformer(
        final ApplicationContext context,
        @Property(name = MicronautAwsProxyConfiguration.PREFIX + ".encode-base64", defaultValue = "false") final boolean encodeBase64) {
        this.context = context;
        this.encodeBase64 = encodeBase64;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public APIGatewayV2HTTPResponse toAwsResponse(final HttpResponse<?> response) {
        HttpHeaders headers = response.getHeaders();
        Map<String, String> responseHeaders = new HashMap<>();
        headers.asMap().entrySet().stream()
            .filter(not(h -> List.of(HttpHeaders.SET_COOKIE, HttpHeaders.SET_COOKIE2).contains(h.getKey())))
            .forEach(entry -> responseHeaders.put(entry.getKey(), String.join(",", entry.getValue())));

        MediaType mediaType = response.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec codec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec not found for type " + mediaType));

        String responseBody = response.getBody()
            .map(body -> LambdaUtils.encodeBody(codec.encode(body), encodeBase64))
            .orElse(null);

        List<String> responseCookies = response.getCookies().asMap().values().stream()
            .map(Cookie::toString)
            .collect(Collectors.toList());

        return APIGatewayV2HTTPResponse.builder()
            .withStatusCode(response.getStatus().getCode())
            .withHeaders(responseHeaders)
            .withCookies(responseCookies)
            .withIsBase64Encoded(encodeBase64)
            .withBody(responseBody)
            .build();
    }
}
