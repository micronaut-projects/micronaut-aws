package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.aws.dynamodb.conf.DynamoConfiguration;
import io.micronaut.aws.dynamodb.utils.TestDynamoRepository;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.util.Arrays;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Singleton
public class BootStrap extends TestDynamoRepository {
    public static final String INDEX_GSI1 = "gsi1";
    public static final String INDEX_GSI1_PK = "gsi1Pk";
    public static final String INDEX_GSI1_SK = "gsi1Sk";

    public static final String INDEX_GSI2 = "gsi2";
    public static final String INDEX_GSI2_PK = "gsi2Pk";
    public static final String INDEX_GSI2_SK = "gsi2Sk";

    public static final String INDEX_GSI3 = "gsi3";
    public static final String INDEX_GSI3_PK = "gsi3Pk";
    public static final String INDEX_GSI3_SK = "gsi3Sk";

    public BootStrap(DynamoDbClient dynamoDbClient,
                     DynamoConfiguration dynamoConfiguration) {
        super(dynamoDbClient, dynamoConfiguration);
    }

    @Override
    public CreateTableRequest createTableRequest(String tableName) {
        return CreateTableRequest.builder()
            .attributeDefinitions(
                attributeDefinition("pk", ScalarAttributeType.S),
                attributeDefinition("sk", ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI1_PK, ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI1_SK, ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI2_PK, ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI2_SK, ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI3_PK, ScalarAttributeType.S),
                attributeDefinition(INDEX_GSI3_SK, ScalarAttributeType.S)
            )
            .keySchema(Arrays.asList(keySchemaElement("pk", KeyType.HASH), keySchemaElement("sk", KeyType.RANGE)))
            .globalSecondaryIndexes(
                gsi(INDEX_GSI1, INDEX_GSI1_PK, INDEX_GSI1_SK),
                gsi(INDEX_GSI2, INDEX_GSI2_PK, INDEX_GSI2_SK),
                gsi(INDEX_GSI3, INDEX_GSI3_PK, INDEX_GSI3_SK)
            )
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .tableName(tableName)
            .build();
    }
}
