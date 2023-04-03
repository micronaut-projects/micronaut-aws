package io.micronaut.aws.dynamodb.ecommerce.items;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.GlobalSecondaryIndex1;
import io.micronaut.aws.dynamodb.SingleTableRowWithOneGlobalSecondaryIndex;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrderItemRow extends SingleTableRowWithOneGlobalSecondaryIndex {

    @NonNull
    @NotBlank
    private final String orderId;

    @NonNull
    @NotBlank
    private final String itemId;

    @NonNull
    @NotBlank
    private final String description;

    @NonNull
    @NotNull
    private final BigDecimal price;

    @NonNull
    @NotNull
    private final Integer amount;

    @NonNull
    @NotNull
    private final BigDecimal totalCost;

    @Creator
    public OrderItemRow(String pk,
                        String sk,
                        String className,
                        String gsi1Pk,
                        String gsi1Sk,
                        String orderId,
                        String itemId,
                        String description,
                        BigDecimal price,
                        Integer amount,
                        BigDecimal totalCost) {
        super(pk, sk, className, gsi1Pk, gsi1Sk);
        this.orderId = orderId;
        this.itemId = itemId;
        this.description = description;
        this.price = price;
        this.amount = amount;
        this.totalCost = totalCost;
    }

    public OrderItemRow(CompositeKey key,
                        GlobalSecondaryIndex1 gsi1,
                        String orderId,
                        String itemId,
                        String description,
                        BigDecimal price,
                        Integer amount,
                        BigDecimal totalCost) {
        this(key.getPartionKey(),
            key.getSortKey(),
            OrderItemRow.class.getName(),
            gsi1.getPartionKey(),
            gsi1.getSortKey(),
            orderId,
            itemId,
            description,
            price,
            amount,
            totalCost);
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

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    @NonNull
    public static CompositeKey keyOf(@NonNull String orderId, String itemId) {
        final String value = "ORDER#" + orderId + "#ITEM#" + itemId;
        return CompositeKey.of(value, value);
    }

    @NonNull
    public static GlobalSecondaryIndex1 gsi1Of(@NonNull String orderId, String itemId) {
        return GlobalSecondaryIndex1.of("ORDER#" + orderId, "ITEM#" + itemId);
    }
}
