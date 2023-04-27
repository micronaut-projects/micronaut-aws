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

import io.micronaut.core.annotation.NonNull;

/**
 * Configuration to define the DynamoDB table name.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public interface DynamoConfiguration {
    /**
     *
     * @return The DynamoDB Table Name
     */
    @NonNull
    String getTableName();

    /**
     *
     * @return The DynamoDB Table Hash Key name
     */
    @NonNull
    String getHashKey();

    /**
     *
     * @return The DynamoDB Table Sort Key name
     */
    @NonNull
    String getSortKey();

    /**
     *
     * @return The DynamoDB Table Hash Key name for the Global Secondary Index 1.
     */
    @NonNull
    String getGlobalSecondaryIndex1HashKey();

    /**
     *
     * @return The DynamoDB Table Sort Key name for the Global Secondary Index 1.
     */
    @NonNull
    String getGlobalSecondaryIndex1SortKey();
}
