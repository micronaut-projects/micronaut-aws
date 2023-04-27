package io.micronaut.aws.dynamodb.ecommerce.items;

import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Introspected
public class CustomerRow extends SingleTableRow {

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

    @Creator
    public CustomerRow(String pk, String sk, String className, String username, String email, String name) {
        super(pk, sk, className);
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public CustomerRow(CompositeKey key, String username, String email, String name) {
        this(key.getPartionKey(), key.getSortKey(), CustomerRow.class.getName(), username, email, name);
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

    @NonNull
    public static CompositeKey keyOf(@NonNull String username) {
        return CompositeKey.of("CUSTOMER#" + username, "CUSTOMER#" + username);
    }
}
