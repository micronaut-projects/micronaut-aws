package io.micronaut.aws.lambda.events;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class FileUtils {

    private FileUtils() {

    }

    public static Optional<String> text(ClassLoader classLoader, String resourceFileName) throws IOException {
        URL resource = classLoader.getResource(resourceFileName);
        if (resource == null) {
            return Optional.empty();
        }
        String json = null;
        try (InputStream inputStream = resource.openStream()) {
            json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        return Optional.ofNullable(json);
    }
}
