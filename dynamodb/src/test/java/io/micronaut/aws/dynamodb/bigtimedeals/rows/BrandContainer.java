package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

import java.util.Collections;
import java.util.Set;

@Introspected
public class BrandContainer extends SingleTableRow {
    public static final CompositeKey KEY = CompositeKey.of("BRANDS", "BRANDS");
    private final Set<String> brands;

    @Creator
    public BrandContainer(String pk, String sk, @Nullable String className, Set<String> brands) {
        super(pk, sk, className);
        this.brands = brands;
    }

    public BrandContainer() {
        this(KEY.getPartionKey(), KEY.getSortKey(), BrandContainer.class.getName(), Collections.emptySet());
    }

    public Set<String> getBrands() {
        return brands;
    }

}
