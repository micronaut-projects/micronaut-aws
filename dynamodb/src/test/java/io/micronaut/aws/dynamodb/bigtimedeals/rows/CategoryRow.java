package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import java.util.Locale;

@Introspected
public class CategoryRow extends SingleTableRow {
    private final String name;
    private final Integer likeCount;
    private final Integer watchCount;

    @Creator
    public CategoryRow(String pk,
                       String sk,
                       String className,
                       String name,
                       Integer likeCount,
                       Integer watchCount) {
        super(pk, sk, className);
        this.name = name;
        this.likeCount = likeCount;
        this.watchCount = watchCount;
    }

    public CategoryRow(CompositeKey key,
                       String name,
                       Integer likeCount,
                       Integer watchCount) {
        this(key.getPartionKey(), key.getSortKey(), CategoryRow.class.getName(), name, likeCount, watchCount);
    }

    public CategoryRow(String name,
                       Integer likeCount,
                       Integer watchCount) {
        this(key(name), name, likeCount, watchCount);
    }

    @NonNull
    public static CompositeKey key(@NonNull String name) {
        final String value = "CATEGORY#" + name.toUpperCase(Locale.ROOT);
        return CompositeKey.of(value, value);
    }

    public String getName() {
        return name;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Integer getWatchCount() {
        return watchCount;
    }
}
