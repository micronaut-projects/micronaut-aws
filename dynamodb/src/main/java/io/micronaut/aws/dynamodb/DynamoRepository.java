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
import io.micronaut.aws.dynamodb.utils.AttributeValueUtils;
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
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;
import software.amazon.awssdk.services.dynamodb.model.Update;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility class to simplifies operations with a {@link DynamoDbClient} working with a DynamoDB table whose name is specified by the bean {@link DynamoConfiguration#getTableName()}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Requires(beans = { DynamoDbClient.class, DynamoConfiguration.class, DynamoDbConversionService.class })
@Singleton
public class DynamoRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoRepository.class);
    private final DynamoDbClient dynamoDbClient;
    private final DynamoConfiguration dynamoConfiguration;

    private final DynamoDbConversionService dynamoDbConversionService;

    protected DynamoRepository(DynamoDbClient dynamoDbClient,
                               DynamoConfiguration dynamoConfiguration,
                               DynamoDbConversionService dynamoDbConversionService) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoConfiguration = dynamoConfiguration;
        this.dynamoDbConversionService = dynamoDbConversionService;
    }

    /**
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
     * @param item DynamoDB Item
     * @return A put Item Response
     */
    @NonNull
    public PutItemResponse putItem(@NonNull Map<String, AttributeValue> item) {
        return putItem(item, null);
    }

    /**
     * @param item            DynamoDB Item
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
     * @return The DynamoDB Client
     */
    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    /**
     * @return DynamoDB Configuration
     */
    public DynamoConfiguration getDynamoConfiguration() {
        return dynamoConfiguration;
    }

    /**
     * @return GetItemRequest Builder with the table name populated via {@link DynamoConfiguration#getTableName()}.
     */
    @NonNull
    public GetItemRequest.Builder getItemBuilder() {
        return GetItemRequest.builder()
            .tableName(dynamoConfiguration.getTableName());
    }

    /**
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
     * @return QueryRequest Builder with the table name populated via {@link DynamoConfiguration#getTableName()}.
     */
    @NonNull
    public QueryRequest.Builder queryRequestBuilder() {
        return QueryRequest.builder()
            .tableName(dynamoConfiguration.getTableName());
    }

    /**
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
     * @param builderConsumer Query Request Builder Consumer
     * @return QueryResponse
     */
    @NonNull
    public QueryResponse query(@Nullable Consumer<QueryRequest.Builder> builderConsumer) {
        return query(queryRequestBuilder(builderConsumer));
    }

    /**
     * @param queryRequest Query Request
     * @return QueryResponse
     */
    @NonNull
    public QueryResponse query(@NonNull QueryRequest queryRequest) {
        return dynamoDbClient.query(queryRequest);
    }

    /**
     * @param putBuilderConsumers PutBuilderConsumers
     * @return The Transaction write Item response
     */
    @NonNull
    public TransactWriteItemsResponse transactWriteItems(@NonNull List<Consumer<Put.Builder>> putBuilderConsumers) {
        return transactWriteItems(putBuilderConsumers.stream().toArray(Consumer[]::new));
    }

    /**
     * @param putBuilderConsumers PutBuilderConsumers
     * @return The Transaction write Item response
     */
    @NonNull
    public TransactWriteItemsResponse transactWriteItems(@NonNull Consumer<Put.Builder>... putBuilderConsumers) {
        TransactWriteItem[] transactWriteItemArr = new TransactWriteItem[putBuilderConsumers.length];
        int count = 0;
        for (Consumer<Put.Builder> putBuilderConsumer : putBuilderConsumers) {
            transactWriteItemArr[count++] = putTransactWriteItem(putBuilderConsumer);
        }
        return dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
            .transactItems(transactWriteItemArr)
            .build());
    }

    /**
     *
     * @param puBuilderConsumer PutBuilderConsumer
     * @return Transaction write item
     */
    public TransactWriteItem putTransactWriteItem(@Nullable Consumer<Put.Builder> puBuilderConsumer) {
        Put.Builder putBuidler = Put.builder()
            .tableName(getDynamoConfiguration().getTableName());
        if (puBuilderConsumer != null) {
            puBuilderConsumer.accept(putBuidler);
        }
        return TransactWriteItem.builder()
            .put(putBuidler.build())
            .build();
    }

    /**
     *
     * @param updateBuilderConsumer updateBuilderConsumer
     * @return Transaction write item
     */
    public TransactWriteItem updateTransactWriteItem(@Nullable Consumer<Update.Builder> updateBuilderConsumer) {
        Update.Builder updateBuilder = Update.builder()
            .tableName(getDynamoConfiguration().getTableName());
        if (updateBuilderConsumer != null) {
            updateBuilderConsumer.accept(updateBuilder);
        }
        return TransactWriteItem.builder()
            .update(updateBuilder.build())
            .build();
    }

    /**
     * @param transactWriteItemArr items to write
     * @return The Transaction write Item response
     */
    @NonNull
    public TransactWriteItemsResponse transactWriteItems(TransactWriteItem... transactWriteItemArr) {
        return dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
            .transactItems(transactWriteItemArr)
            .build());

    }

    /**
     *
     * @param key Table Key
     * @param targetType Target Type class
     * @return An Optional Instance of the Target class if found.
     * @param <T> Target Type
     */
    public <T> Optional<T> getItem(CompositeKey key, Class<T> targetType) {
        Map<String, AttributeValue> keyMap = mapForKey(key);
        return getItem(keyMap, targetType);
    }

    /**
     *
     * @param key Key
     * @return Key Map
     */
    @NonNull
    public Map<String, AttributeValue> mapForKey(@NonNull CompositeKey key) {
        if (key instanceof GlobalSecondaryIndex1) {
            return Map.of(dynamoConfiguration.getGlobalSecondaryIndex1HashKey(), AttributeValueUtils.s(key.getPartionKey()),
                dynamoConfiguration.getGlobalSecondaryIndex1SortKey(), AttributeValueUtils.s(key.getSortKey()));
        }
        return Map.of(dynamoConfiguration.getHashKey(), AttributeValueUtils.s(key.getPartionKey()),
            dynamoConfiguration.getSortKey(), AttributeValueUtils.s(key.getSortKey()));
    }

    /**
     *
     * @param key Table Key
     * @param targetType Target Type class
     * @return An Optional Instance of the Target class if found.
     * @param <T> Target Type
     */
    public <T> Optional<T> getItem(Map<String, AttributeValue> key, Class<T> targetType) {
        GetItemResponse response = getItem(builder -> builder.key(key));
        if (!response.hasItem()) {
            return Optional.empty();
        }
        Map<String, AttributeValue> item = response.item();
        if (item == null) {
            return Optional.empty();
        }
        return Optional.of(dynamoDbConversionService.convert(item, targetType));
    }

    /**
     *
     * @return DynamoDB Conversion service.
     */
    public DynamoDbConversionService getDynamoDbConversionService() {
        return dynamoDbConversionService;
    }

    /**
     * @param key compositeKey
     * @param updateItemRequestBuilderConsumer Update Item Request Builder Consumer
     * @return Update Item Response
     */
    @NonNull
    public UpdateItemResponse updateItem(@NonNull CompositeKey key, @NonNull Consumer<UpdateItemRequest.Builder> updateItemRequestBuilderConsumer) {
        return dynamoDbClient.updateItem(updateItemRequestBuilder(key, updateItemRequestBuilderConsumer).build());
    }

    /**
     *
     * @param updateItemRequestBuilderConsumer Update Item Request Builder Consumer
     * @return Update Item Response
     */
    @NonNull
    public UpdateItemResponse updateItem(@NonNull Consumer<UpdateItemRequest.Builder> updateItemRequestBuilderConsumer) {
        return dynamoDbClient.updateItem(updateItemRequestBuilder(updateItemRequestBuilderConsumer).build());
    }


    /**
     *
     * @param updateItemRequestBuilder Update Item Request Builder
     * @return Update Item Response
     */
    @NonNull
    public UpdateItemResponse updateItem(@NonNull UpdateItemRequest.Builder updateItemRequestBuilder) {
        return dynamoDbClient.updateItem(updateItemRequestBuilder.build());
    }

    /**
     * Instantiates UpdateItem Request Builder, populates its table name with {@link DynamoConfiguration#getTableName()}.
     * @return UpdateItem Request Builder
     */
    @NonNull
    public UpdateItemRequest.Builder updateItemRequestBuilder() {
        return UpdateItemRequest.builder()
            .tableName(dynamoConfiguration.getTableName());
    }

    /**
     * Instantiates UpdateItem Request Builder, populates its table name with {@link DynamoConfiguration#getTableName()} and passes it to the consumer.
     * @param builderConsumer UpdateItem Request Builder Consumer
     * @return UpdateItem Request Builder
     */
    @NonNull
    public UpdateItemRequest.Builder updateItemRequestBuilder(@Nullable Consumer<UpdateItemRequest.Builder> builderConsumer) {
        UpdateItemRequest.Builder builder =  updateItemRequestBuilder();
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        return builder;
    }

    /**
     * Instantiates UpdateItem Request Builder, populates its table name with {@link DynamoConfiguration#getTableName()} and passes it to the consumer.
     * @param key Composite Key
     * @param builderConsumer UpdateItem Request Builder Consumer
     * @return UpdateItem Request Builder
     */
    @NonNull
    public UpdateItemRequest.Builder updateItemRequestBuilder(@NonNull CompositeKey key, @Nullable Consumer<UpdateItemRequest.Builder> builderConsumer) {
        UpdateItemRequest.Builder builder =  updateItemRequestBuilder();
        builder.key(mapForKey(key));
        if (builderConsumer != null) {
            builderConsumer.accept(builder);
        }
        return builder;
    }
}
