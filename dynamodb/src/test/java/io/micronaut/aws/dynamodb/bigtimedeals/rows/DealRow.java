package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.SingleTableRowWithThreeGlobalSecondaryIndex;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Introspected
public class DealRow extends SingleTableRowWithThreeGlobalSecondaryIndex {
    private final String dealId;
    private final String title;
    private final String link;
    private final BigDecimal price;
    private final String category;
    private final String brand;
    private final LocalDateTime createdAt;

    @Creator
    public DealRow(String pk,
                   String sk,
                   String className,
                   String gsi1Pk,
                   String gsi1Sk,
                   String gsi2Pk,
                   String gsi2Sk,
                   String gsi3Pk,
                   String gsi3Sk,
                   String dealId,
                   String title,
                   String link,
                   BigDecimal price,
                   String category,
                   String brand,
                   LocalDateTime createdAt) {
        super(pk, sk, className, gsi1Pk, gsi1Sk, gsi2Pk, gsi2Sk, gsi3Pk, gsi3Sk);
        this.dealId = dealId;
        this.title = title;
        this.link = link;
        this.price = price;
        this.category = category;
        this.brand = brand;
        this.createdAt = createdAt;
    }

    public DealRow(CompositeKey key,
                   @Nullable CompositeKey gsi1,
                   @Nullable CompositeKey gsi2,
                   @Nullable CompositeKey gsi3,
                   String dealId,
                   String title,
                   String link,
                   BigDecimal price,
                   String category,
                   String brand,
                   LocalDateTime createdAt) {
        this(key.getPartionKey(),
            key.getSortKey(),
            DealRow.class.getName(),
            gsi1 == null ? null : gsi1.getPartionKey(),
            gsi1 == null ? null : gsi1.getSortKey(),
            gsi2 == null ? null : gsi2.getPartionKey(),
            gsi2 == null ? null : gsi2.getSortKey(),
            gsi3 == null ? null : gsi3.getPartionKey(),
            gsi3 == null ? null : gsi3.getSortKey(),
            dealId,
            title,
            link,
            price,
            category,
            brand,
            createdAt);
    }

    public DealRow(String dealId,
                   String title,
                   String link,
                   BigDecimal price,
                   String category,
                   String brand,
                   LocalDateTime createdAt) {
        this(DealRow.key(dealId),
            DealRow.gsi1(dealId, createdAt),
            DealRow.gsi2(dealId, createdAt, brand).orElse(null),
            DealRow.gsi3(dealId, createdAt, category).orElse(null),
            dealId,
            title,
            link,
            price,
            category,
            brand,
            createdAt);
    }

    public String getDealId() {
        return dealId;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @NonNull
    public static CompositeKey key(@NonNull String dealId) {
        final String value = "DEAL#"+ dealId;
        return CompositeKey.of(value, value);
    }

    @NonNull
    public static CompositeKey gsi1(@NonNull String dealId, @NonNull LocalDateTime createdAt) {
        return CompositeKey.of("DEALS#" + truncateTimestamp(createdAt).toString(), "DEAL#" + dealId);
    }

    @NonNull
    public static Optional<CompositeKey> gsi2(@NonNull String dealId, @NonNull LocalDateTime createdAt, @Nullable String brand) {
        if (brand == null) {
            return Optional.empty();
        }
        return Optional.of(CompositeKey.of("BRAND#" +  brand.toUpperCase() + "#" + truncateTimestamp(createdAt).toString(), "DEAL#" + dealId));
    }

    @NonNull
    public static Optional<CompositeKey> gsi3(@NonNull String dealId, @NonNull LocalDateTime createdAt, @Nullable String category) {
        if (category == null) {
            return Optional.empty();
        }
        return Optional.of(CompositeKey.of("CATEGORY#" + category.toUpperCase() + "#" +  truncateTimestamp(createdAt).toString(), "DEAL#" + dealId));
    }

    @NonNull
    public static LocalDateTime truncateTimestamp(@NonNull LocalDateTime timestamp) {
        return LocalDateTime.of(timestamp.toLocalDate(), LocalTime.of(0, 0, 0, 0));
    }
}
