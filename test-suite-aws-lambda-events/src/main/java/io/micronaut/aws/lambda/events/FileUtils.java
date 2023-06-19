package io.micronaut.aws.lambda.events;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class FileUtils {

    private FileUtils() {
    }

    public static Optional<String> text(ClassLoader classLoader, String resourceFileName) throws IOException {
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceFileName)) {
            return inputStream == null ? Optional.empty() : Optional.of(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
