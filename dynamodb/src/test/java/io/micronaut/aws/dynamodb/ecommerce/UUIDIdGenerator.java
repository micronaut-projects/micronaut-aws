package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class UUIDIdGenerator implements IdGenerator {
    @Override
    @NonNull
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
