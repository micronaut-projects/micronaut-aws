package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Serdeable
public class OrderItem {
    @NonNull
    @NotBlank
    private final String orderId;

    @NonNull
    @NotBlank
    private final String itemId;

    @NotBlank
    @NonNull
    private final String description;

    @NonNull
    @NotNull
    private final BigDecimal price;

    @NonNull
    @NotNull
    private final Integer amount;

    public OrderItem(String orderId,
              String itemId, String description, BigDecimal price, Integer amount) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getAmount() {
        return amount;
    }
}
