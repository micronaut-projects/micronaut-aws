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
package io.micronaut.aws.dynamodb;

import io.micronaut.aws.dynamodb.conf.DynamoConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.Map;
import java.util.function.Consumer;

@Requires(beans = { DynamoDbClient.class, DynamoConfiguration.class })
@Singleton
public class DynamoRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoRepository.class);
    private final DynamoDbClient dynamoDbClient;
    private final DynamoConfiguration dynamoConfiguration;

    protected DynamoRepository(DynamoDbClient dynamoDbClient,
                               DynamoConfiguration dynamoConfiguration) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoConfiguration = dynamoConfiguration;
    }

    /**
     *
     * @param item DynamoDB Item
     * @return A PutItem Request builder with the table name populated via {@link DynamoConfiguration#getTableName()}.
     */
    @NonNull
    public PutItemRequest.Builder putItemRequest(@NonNull Map<String, AttributeValue> item) {
        return PutItemRequest.builder()
            .tableName(dynamoConfiguration.getTableName())
            .item(item);
    }

    /**
     *
     * @param item DynamoDB Item
     * @return A put Item Response
     */
    @NonNull
    public PutItemResponse putItem(@NonNull Map<String, AttributeValue> item) {
        return putItem(item, null);
    }

    /**
     *
     * @param item DynamoDB Item
     * @param builderConsumer PutItem Request Builder consumer
     * @return A put Item Response
     */
    public PutItemResponse putItem(@NonNull Map<String, AttributeValue> item, @Nullable Consumer<PutItemRequest.Builder> builderConsumer) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("****************************");
            for (String k : item.keySet()) {
                LOG.trace("{}: {}", k, item.get(k));
            }
            LOG.trace("****************************");
        }
        PutItemRequest.Builder builder = putItemRequest(item);
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        return putItem(builder.build());
    }

    /**
     *
     * @param putItemRequest PutItem REquest
     * @return A PutItem Response
     */
    @NonNull
    public PutItemResponse putItem(@NonNull PutItemRequest putItemRequest) {
        PutItemResponse itemResponse = dynamoDbClient.putItem(putItemRequest);
        if (LOG.isTraceEnabled()) {
            LOG.trace("{}", itemResponse);
        }
        return itemResponse;
    }

    /**
     *
     * @return The DynamoDB Client
     */
    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    /**
     *
     * @return DynamoDB Configuration
     */
    public DynamoConfiguration getDynamoConfiguration() {
        return dynamoConfiguration;
    }

    /**
     *
     * @return GetItemRequest Builder with the table name populated via {@link DynamoConfiguration#getTableName()}.
     */
    @NonNull
    public GetItemRequest.Builder getItemBuilder() {
        return GetItemRequest.builder()
            .tableName(dynamoConfiguration.getTableName());
    }

    /**
     *
     * @param builderConsumer GetItemRequest Builder consumer
     * @return Get Item Response
     */
    public GetItemResponse getItem(@Nullable Consumer<GetItemRequest.Builder> builderConsumer) {
        GetItemRequest.Builder builder = getItemBuilder();
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        return dynamoDbClient.getItem(builder.build());
    }

    /**
     *
     * @return QueryRequest Builder with the table name populated via {@link DynamoConfiguration#getTableName()}.
     */
    @NonNull
    public QueryRequest.Builder queryRequestBuilder() {
        return QueryRequest.builder()
            .tableName(dynamoConfiguration.getTableName());
    }

    /**
     *
     * @param builderConsumer Query Request Builder Consumer
     * @return Query Request
     */
    @NonNull
    public QueryRequest queryRequestBuilder(@Nullable Consumer<QueryRequest.Builder> builderConsumer) {
        QueryRequest.Builder builder = queryRequestBuilder();
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        return builder.build();
    }

    /**
     *
     * @param queryRequest Query Request
     * @return QueryResponse
     */
    @NonNull
    public QueryResponse query(@NonNull QueryRequest queryRequest) {
        return dynamoDbClient.query(queryRequest);
    }
}
