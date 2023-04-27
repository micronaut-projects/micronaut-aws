package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Serdeable
public class CreateOrder {
    @NotNull
    @NonNull
    @Valid
    private final Address address;

    @Size(min = 1)
    @NotNull
    @NonNull
    private final List<@Valid Item> items;

    public CreateOrder(@NonNull Address address,
                       @NonNull List<@Valid Item> items) {
        this.address = address;
        this.items = items;
    }

    @NonNull
    public Address getAddress() {
        return address;
    }

    @NonNull
    public List<Item> getItems() {
        return items;
    }
}
