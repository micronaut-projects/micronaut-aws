package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Serdeable
public class Customer {
    @NonNull
    @NotBlank
    private final String username;

    @NonNull
    @NotBlank
    @Email
    private final String email;

    @NonNull
    @NotBlank
    private final String name;

    @Nullable
    private List<Address> addresses;

    public Customer(@NonNull String username,
                    @NonNull String email,
                    @NonNull String name,
                    @Nullable List<Address> addresses) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.addresses = addresses;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public List<Address> getAddresses() {
        return addresses;
    }
}
