package io.micronaut.aws.dynamodb.utils;

import io.micronaut.core.util.CollectionUtils;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class DynamoDbLocal implements AutoCloseable {

    private static GenericContainer dynamoDBLocal;

    private static GenericContainer getDynamoDBLocal() {
        if (dynamoDBLocal == null) {
            dynamoDBLocal = new GenericContainer("amazon/dynamodb-local")
                .withExposedPorts(8000);
            dynamoDBLocal.start();
        }
        return dynamoDBLocal;
    }

    public static Map<String, String> getProperties() {
        return CollectionUtils.mapOf(
            "dynamodb-local.host", "localhost",
            "dynamodb-local.port", getDynamoDBLocal().getFirstMappedPort());

    }

    public static void shutdown() {
        if (dynamoDBLocal != null) {
            dynamoDBLocal.close();
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }
}
