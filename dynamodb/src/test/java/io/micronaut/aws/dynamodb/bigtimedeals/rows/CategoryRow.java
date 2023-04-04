package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.SingleTableRow;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class CategoryRow extends SingleTableRow {
    /**
     * @param pk        Primary Key
     * @param sk        Sort Key
     * @param className Class Name
     */
    public CategoryRow(String pk, String sk, String className) {
        super(pk, sk, className);
    }
}
