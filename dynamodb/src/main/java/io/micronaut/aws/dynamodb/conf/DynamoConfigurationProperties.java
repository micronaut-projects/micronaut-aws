/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.dynamodb.conf;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;

/**
 * {@link ConfigurationProperties} implementation of {@link DynamoConfiguration}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Requires(property = "dynamodb.table-name")
@ConfigurationProperties("dynamodb")
public class DynamoConfigurationProperties implements DynamoConfiguration {

    public static final String DEFAULT_HASH_KEY = "pk";
    public static final String DEFAULT_SORT_KEY = "sk";
    public static final String DEFAULT_GSI1_HASH_KEY = "gsi1pk";
    public static final String DEFAULT_GSI1_SORT_KEY = "gsi1sk";

    @NonNull
    @NotBlank
    private String tableName;

    @NonNull
    @NotBlank
    private String hashKey = DEFAULT_HASH_KEY;

    @NonNull
    @NotBlank
    private String sortKey = DEFAULT_SORT_KEY;

    @NonNull
    @NotBlank
    private String globalSecondaryIndex1HashKey = DEFAULT_GSI1_HASH_KEY;

    @NonNull
    @NotBlank
    private String globalSecondaryIndex1SortKey = DEFAULT_GSI1_SORT_KEY;

    /**
     *
     * @param tableName The DynamoDB table name
     */
    public void setTableName(@NonNull String tableName) {
        this.tableName = tableName;
    }

    @Override
    @NonNull
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getHashKey() {
        return hashKey;
    }

    /**
     *
     * @param hashKey The Table hash Key name. Default value: {@value #DEFAULT_HASH_KEY}.
     */
    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    @Override
    public String getSortKey() {
        return sortKey;
    }

    /**
     *
     * @param sortKey The Table hash Key name. Default value: {@value #DEFAULT_SORT_KEY}.
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public String getGlobalSecondaryIndex1HashKey() {
        return globalSecondaryIndex1HashKey;
    }

    /**
     *
     * @param globalSecondaryIndex1HashKey Global Secondary Index 1 hash Key name. Default value: {@value #DEFAULT_GSI1_HASH_KEY}.
     */
    public void setGlobalSecondaryIndex1HashKey(String globalSecondaryIndex1HashKey) {
        this.globalSecondaryIndex1HashKey = globalSecondaryIndex1HashKey;
    }

    @Override
    public String getGlobalSecondaryIndex1SortKey() {
        return globalSecondaryIndex1SortKey;
    }

    /**
     *
     * @param globalSecondaryIndex1SortKey Global Secondary Index 1 hash Key name. Default value: {@value #DEFAULT_GSI1_SORT_KEY}.
     */
    public void setGlobalSecondaryIndex1SortKey(String globalSecondaryIndex1SortKey) {
        this.globalSecondaryIndex1SortKey = globalSecondaryIndex1SortKey;
    }
}

