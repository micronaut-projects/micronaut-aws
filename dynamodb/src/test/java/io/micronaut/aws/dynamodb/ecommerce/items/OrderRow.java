package io.micronaut.aws.dynamodb.ecommerce.items;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.GlobalSecondaryIndex1;
import io.micronaut.aws.dynamodb.SingleTableRowWithOneGlobalSecondaryIndex;
import io.micronaut.aws.dynamodb.ecommerce.Address;
import io.micronaut.aws.dynamodb.ecommerce.Status;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Introspected
public class OrderRow extends SingleTableRowWithOneGlobalSecondaryIndex {

    private final String type;

    private final String username;
    private final String orderId;

    private final Address address;

    private final LocalDateTime createdAt;

    private final Status status;

    private final BigDecimal totalAmount;

    private final Integer numberItems;

    @Creator
    public OrderRow(String pk,
                             String sk,
                             String className,
                             String gsi1Pk,
                             String gsi1Sk,
                             String type,
                             String username,
                             String orderId,
                             Address address,
                             LocalDateTime createdAt,
                             Status status,
                             BigDecimal totalAmount,
                             Integer numberItems) {
        super(pk, sk, className, gsi1Pk, gsi1Sk);
        this.type = type;
        this.username = username;
        this.orderId = orderId;
        this.address = address;
        this.createdAt = createdAt;
        this.status = status;
        this.totalAmount = totalAmount;
        this.numberItems = numberItems;
    }

    public OrderRow(CompositeKey key,
                             GlobalSecondaryIndex1 gsi1,
                             String username,
                             String orderId,
                             Address address,
                             LocalDateTime createdAt,
                             Status status,
                             BigDecimal totalAmount,
                             Integer numberItems) {
        this(key.getPartionKey(), key.getSortKey(), OrderRow.class.getName(), gsi1.getPartionKey(), gsi1.getSortKey(),
            OrderRow.class.getName(),
            username,
            orderId,
            address,
            createdAt,
            status,
            totalAmount,
            numberItems);
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getOrderId() {
        return orderId;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Integer getNumberItems() {
        return numberItems;
    }

    @NonNull
    public static CompositeKey keyOf(@NonNull String username, @NonNull String orderId) {
        return CompositeKey.of("CUSTOMER#" + username, "ORDER#" + orderId);
    }

    @NonNull
    public static GlobalSecondaryIndex1 gsi1Of(@NonNull String orderId) {
        return GlobalSecondaryIndex1.of("ORDER#" + orderId, "ORDER#" + orderId);
    }

}
