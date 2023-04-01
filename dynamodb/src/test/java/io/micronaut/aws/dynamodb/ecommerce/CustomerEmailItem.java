package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.BaseItem;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CustomerEmailItem extends BaseItem {
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        public CustomerEmailItem(String pk, String sk, String username, String email) {
            super(pk, sk);
            this.username = username;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
}
