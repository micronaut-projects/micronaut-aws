package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;

@DefaultImplementation(UUIDIdGenerator.class)
public interface IdGenerator {

    @NonNull
    String generate();
}
