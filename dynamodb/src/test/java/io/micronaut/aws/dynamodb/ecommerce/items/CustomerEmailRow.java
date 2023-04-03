package io.micronaut.aws.dynamodb.ecommerce.items;

import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CustomerEmailRow extends SingleTableRow {
    @NonNull
    @NotBlank
    private final String username;

    @NonNull
    @NotBlank
    @Email
    private final String email;

    @Creator
    public CustomerEmailRow(String pk, String sk, String className, String username, String email) {
        super(pk, sk, className);
        this.username = username;
        this.email = email;
    }

    public CustomerEmailRow(CompositeKey key, String username, String email) {
        this(key.getPartionKey(), key.getSortKey(), CustomerEmailRow.class.getName(), username, email);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    @NonNull
    public static CompositeKey keyOf(@NonNull String email) {
        return CompositeKey.of("CUSTOMEREMAIL#" + email,
                "CUSTOMEREMAIL#" + email);
    }
}
