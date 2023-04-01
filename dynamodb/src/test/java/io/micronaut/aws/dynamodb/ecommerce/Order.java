package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.core.annotation.Introspected;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Introspected
public class Order {
    private final String username;
    private final String orderId;
    private final Address address;
    private final LocalDateTime createdAt;
    private final Status status;
    private final BigDecimal totalAmount;
    private final Integer numberOfTimes;

    private final List<OrderItem> items;

    public Order(String username,
                 String orderId,
                 Address address,
                 LocalDateTime createdAt,
                 Status status,
                 BigDecimal totalAmount,
                 Integer numberOfTimes,
                 List<OrderItem> items) {
        this.username = username;
        this.orderId = orderId;
        this.address = address;
        this.createdAt = createdAt;
        this.status = status;
        this.totalAmount = totalAmount;
        this.numberOfTimes = numberOfTimes;
        this.items = items;
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

    public Integer getNumberOfTimes() {
        return numberOfTimes;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
