package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Introspected
public class CustomerEmail {
    @NonNull
    @NotBlank
    private final String username;

    @NonNull
    @NotBlank
    @Email
    private final String email;

    CustomerEmail(String username, String email) {
        this.username = username;
        this.email = email;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getEmail() {
        return email;
    }
}
