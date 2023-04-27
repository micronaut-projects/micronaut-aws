package io.micronaut.aws.dynamodb.utils;

import io.micronaut.aws.dynamodb.conf.DynamoConfiguration;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import java.util.Arrays;
import java.util.Collections;

public abstract class TestDynamoRepository {
    public static final String ATTRIBUTE_PK = "pk";

    protected final DynamoDbClient dynamoDbClient;
    protected final DynamoConfiguration dynamoConfiguration;

    public TestDynamoRepository(DynamoDbClient dynamoDbClient,
                                DynamoConfiguration dynamoConfiguration) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoConfiguration = dynamoConfiguration;
    }


    public boolean existsTable() {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(dynamoConfiguration.getTableName())
                    .build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public void createTable() {
        dynamoDbClient.createTable(createTableRequest(dynamoConfiguration.getTableName()));
    }

    public abstract CreateTableRequest createTableRequest(String tableName);


    public static KeySchemaElement keySchemaElement(String attributeName, KeyType keyType) {
        return KeySchemaElement.builder()
            .attributeName(attributeName)
            .keyType(keyType)
            .build();
    }

    public static AttributeDefinition attributeDefinition(String attributeName, ScalarAttributeType attributeType) {
        return AttributeDefinition.builder()
            .attributeName(attributeName)
            .attributeType(attributeType)
            .build();
    }

    public static GlobalSecondaryIndex gsi(String indexName,
                                            String pkAttributeName,
                                            String skAttributeName) {
        return GlobalSecondaryIndex.builder()
            .indexName(indexName)
            .keySchema(KeySchemaElement.builder()
                .attributeName(pkAttributeName)
                .keyType(KeyType.HASH)
                .build(), KeySchemaElement.builder()
                .attributeName(skAttributeName)
                .keyType(KeyType.RANGE)
                .build())
            .projection(Projection.builder()
                .projectionType(ProjectionType.ALL)
                .build())
            .build();
    }

    public static GlobalSecondaryIndex gsi(String indexName,
                                           String pkAttributeName,
                                           ProjectionType projectionType) {
        return GlobalSecondaryIndex.builder()
            .indexName(indexName)
            .keySchema(KeySchemaElement.builder()
                .attributeName(pkAttributeName)
                .keyType(KeyType.HASH)
                .build())
            .projection(Projection.builder()
                .projectionType(projectionType)
                .build())
            .build();
    }
}
