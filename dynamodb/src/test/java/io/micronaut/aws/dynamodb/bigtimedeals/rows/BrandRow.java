package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import java.util.Locale;

@Introspected
public class BrandRow extends SingleTableRow {
    private final String brandName;
    private final String logoUrl;
    private final Integer likeCount;
    private final Integer watchCount;

    public BrandRow(String pk,
                    String sk,
                    String className,
                    String brandName,
                    String logoUrl,
                    Integer likeCount,
                    Integer watchCount) {
        super(pk, sk, className);
        this.brandName = brandName;
        this.logoUrl = logoUrl;
        this.likeCount = likeCount;
        this.watchCount = watchCount;
    }

    public BrandRow(CompositeKey key,
                    String brandName,
                    String logoUrl) {
        this(key.getPartionKey(), key.getSortKey(), BrandRow.class.getName(), brandName, logoUrl, 0, 0);
    }

    public String getBrandName() {
        return brandName;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Integer getWatchCount() {
        return watchCount;
    }

    @NonNull
    public static CompositeKey key(@NonNull String brandName) {
        final String value = "BRAND#" + brandName.toUpperCase(Locale.ROOT);
        return CompositeKey.of(value, value);
    }
}
