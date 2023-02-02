package io.micronaut.function.aws.proxy.transformer.alb;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
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
import jakarta.inject.Singleton;

@Singleton
public class MicronautAwsAlbResponseTransformer implements MicronautAwsResponseTransformer<ApplicationLoadBalancerResponseEvent> {

    private final ApplicationContext context;
    private final boolean encodeBase64;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public MicronautAwsAlbResponseTransformer(
        final ApplicationContext context,
        @Property(name = MicronautAwsProxyConfiguration.PREFIX + ".encode-base64", defaultValue = "false") final boolean encodeBase64) {
        this.context = context;
        this.encodeBase64 = encodeBase64;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public ApplicationLoadBalancerResponseEvent toAwsResponse(final HttpResponse<?> response) {
        ApplicationLoadBalancerResponseEvent result = new ApplicationLoadBalancerResponseEvent();

        HttpHeaders headers = response.getHeaders();
        Map<String, String> responseHeaders = new HashMap<>();
        headers.asMap().entrySet().stream()
            .forEach(entry -> responseHeaders.put(entry.getKey(), String.join(",", entry.getValue())));

        MediaType mediaType = response.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec codec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec not found for type " + mediaType));

        String responseBody = response.getBody()
            .map(body -> LambdaUtils.encodeBody(codec.encode(body), encodeBase64))
            .orElse(null);

        result.setStatusCode(response.getStatus().getCode());
        result.setBody(responseBody);
        result.setIsBase64Encoded(encodeBase64);
        result.setStatusDescription(response.getStatus().getReason());
        result.setHeaders(responseHeaders);

        return result;
    }
}
