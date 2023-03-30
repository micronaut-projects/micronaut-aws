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

    @NonNull
    @NotBlank
    private String tableName;

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
}

