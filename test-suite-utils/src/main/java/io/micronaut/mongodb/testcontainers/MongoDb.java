package io.micronaut.mongodb.testcontainers;

import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class MongoDb {
    private static final String IMAGE_NAME = "mongo:5";
    private static MongoDBContainer container;

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new MongoDBContainer(DockerImageName.parse(IMAGE_NAME));
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(MongoDBContainer container) {
        return Map.of(
            "mongodb.uri", container.getReplicaSetUrl()
        );
    }
}
