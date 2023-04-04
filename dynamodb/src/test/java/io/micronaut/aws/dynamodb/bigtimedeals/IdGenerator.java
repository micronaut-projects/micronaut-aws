package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.core.annotation.NonNull;

public interface IdGenerator {

    @NonNull
    String generate();
}
