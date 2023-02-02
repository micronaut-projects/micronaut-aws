package io.micronaut.function.aws.proxy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import io.micronaut.core.util.StringUtils;

public class LambdaUtils {
    private LambdaUtils() {

    }

    public static String decodeBody(final String body, final boolean isBase64Encoded) {
        if (isBase64Encoded) {
            byte[] decodedBytes = Base64.getDecoder().decode(body);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

        return body;
    }

    public static String encodeBody(final String body, final boolean isBase64Encoded) {
        return Optional.ofNullable(body)
            .filter(StringUtils::isNotEmpty)
            .map(b -> encodeBody(body.getBytes(StandardCharsets.UTF_8), isBase64Encoded))
            .orElse(null);
    }

    public static String encodeBody(final byte[] body, final boolean isBase64Encoded) {
        if (isBase64Encoded) {
            return Base64.getEncoder().encodeToString(body);
        }

        return new String(body);
    }
}
