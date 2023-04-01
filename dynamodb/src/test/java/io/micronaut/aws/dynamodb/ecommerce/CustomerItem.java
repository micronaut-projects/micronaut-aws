package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.BaseItem;
import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CustomerItem extends BaseItem {

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
    public CustomerItem(String pk, String sk, String username, String email, String name) {
        super(pk, sk);
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public CustomerItem(CompositeKey key, String username, String email, String name) {
        super(key.getPk(), key.getSk());
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
