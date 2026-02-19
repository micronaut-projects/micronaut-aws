package io.micronaut.localstack.testcontainers;

import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class Localstack {
    private static final String IMAGE_NAME = "localstack/localstack:4.9.2";
    private static LocalStackContainer container;

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public static Map<String, String> getProperties(String... services) {
        LocalStackContainer container = getLocalStackContainer(services);
        return Map.of("aws.access-key-id", container.getAccessKey(),
            "aws.secret-key", container.getSecretKey(),
            "aws.region", container.getRegion(),
            "aws.services.lambda.endpoint-override", container.getEndpoint().toString());
    }

    public static LocalStackContainer getLocalStackContainer(String... services) {
        if (container == null) {
            container = new LocalStackContainer(DockerImageName.parse(IMAGE_NAME)).withServices(services);
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return container;
        } else {
            return container;
        }
    }
}
