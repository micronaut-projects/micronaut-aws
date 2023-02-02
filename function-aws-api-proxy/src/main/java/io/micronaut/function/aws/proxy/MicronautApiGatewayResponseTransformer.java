package io.micronaut.function.aws.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import jakarta.inject.Singleton;

@Singleton
@Introspected
public class MicronautApiGatewayResponseTransformer<T> implements MicronautAwsResponseTransformer<APIGatewayProxyResponseEvent> {
    private final ApplicationContext context;
    private final boolean encodeBase64;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public MicronautApiGatewayResponseTransformer(
        final ApplicationContext context,
        @Property(name = MicronautAwsProxyConfiguration.PREFIX + ".encode-base64", defaultValue = "false") final boolean encodeBase64) {
        this.context = context;
        this.encodeBase64 = encodeBase64;
        this.mediaTypeCodecRegistry = context.getBean(MediaTypeCodecRegistry.class);
    }

    @Override
    public APIGatewayProxyResponseEvent toAwsResponse(final HttpResponse<?> response) {
        APIGatewayProxyResponseEvent result = new APIGatewayProxyResponseEvent();

        HttpHeaders headers = response.getHeaders();
        Map<String, List<String>> multiValueHeaders = new HashMap<>();
        Map<String, String> simpleHeaders = new HashMap<>();

        headers.forEach((k, mv) -> {
            if (mv.size() == 1) {
                simpleHeaders.put(k, mv.get(0));
                return;
            }
            multiValueHeaders.put(k, mv);
        });

        MediaType mediaType = response.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);

        MediaTypeCodec codec = mediaTypeCodecRegistry.findCodec(mediaType)
            .orElseThrow(() -> new CodecException("Codec not found for type " + mediaType));

        String responseBody = response.getBody()
            .map(body -> LambdaUtils.encodeBody(codec.encode(body), encodeBase64))
            .orElse(null);

        result.setStatusCode(response.getStatus().getCode());
        result.setHeaders(simpleHeaders);
        result.setMultiValueHeaders(multiValueHeaders);
        result.setBody(responseBody);
        result.setIsBase64Encoded(encodeBase64);

        return result;
    }
}
