package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Singleton
public class DealsRepository {

    void save(@NonNull @NotNull @Valid CreateDeal deal) {

    }
}
