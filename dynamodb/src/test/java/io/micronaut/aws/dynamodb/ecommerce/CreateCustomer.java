package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CreateCustomer {
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


    CreateCustomer(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
