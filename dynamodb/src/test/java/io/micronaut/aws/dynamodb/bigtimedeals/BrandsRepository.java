package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.DynamoRepository;
import io.micronaut.aws.dynamodb.bigtimedeals.rows.BrandContainer;
import io.micronaut.aws.dynamodb.bigtimedeals.rows.BrandRow;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Map;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Singleton
public class BrandsRepository {

    private final DynamoRepository dynamoRepository;

    public BrandsRepository(DynamoRepository dynamoRepository) {
        this.dynamoRepository = dynamoRepository;
    }

    public void save(@NonNull @NotNull @Valid CreateBrand brand) {

        BrandRow brandRow = new BrandRow(BrandRow.key(brand.getName()), brand.getName(), brand.getLogoUrl());

        Map<String, AttributeValue> brandRowMap = dynamoRepository.getDynamoDbConversionService().convert(brandRow);
        dynamoRepository.transactWriteItems(
            dynamoRepository.putTransactWriteItem(builder ->
                builder.item(brandRowMap)
                    .conditionExpression("attribute_not_exists(pk)")
            )
            ,
            dynamoRepository.updateTransactWriteItem(builder ->
                builder.key(dynamoRepository.mapForKey(BrandContainer.KEY))
                .updateExpression("ADD #brands :brand")
                .expressionAttributeNames(Collections.singletonMap("#brands", "brands"))
                .expressionAttributeValues(Collections.singletonMap(":brand", AttributeValue.builder().ss(brand.getName()).build()))
            )
        );
    }
}
