package io.micronaut.aws.dynamodb.utils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DynamoDBLocalExtension implements AfterAllCallback {
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("Shutting down dynamo");
        DynamoDbLocal.shutdown();
    }
}
