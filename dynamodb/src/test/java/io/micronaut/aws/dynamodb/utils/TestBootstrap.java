package io.micronaut.aws.dynamodb.utils;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;

@Requires(property = "dynamodb-local.host")
@Requires(property = "dynamodb-local.port")
@Singleton
public class TestBootstrap implements ApplicationEventListener<StartupEvent> {

    private final TestDynamoRepository dynamoRepository;

    public TestBootstrap(TestDynamoRepository dynamoRepository) {
        this.dynamoRepository = dynamoRepository;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        if (!dynamoRepository.existsTable()) {
            dynamoRepository.createTable();
        }
    }
}

