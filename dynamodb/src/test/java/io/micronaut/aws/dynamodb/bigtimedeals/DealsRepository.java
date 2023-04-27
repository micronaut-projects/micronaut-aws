package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.aws.dynamodb.DynamoRepository;
import io.micronaut.aws.dynamodb.bigtimedeals.rows.DealRow;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Singleton
public class DealsRepository {
    private final IdGenerator idGenerator;
    private final DynamoRepository dynamoRepository;

    public DealsRepository(IdGenerator idGenerator,
                           DynamoRepository dynamoRepository) {
        this.idGenerator = idGenerator;
        this.dynamoRepository = dynamoRepository;
    }

    void save(@NonNull @NotNull @Valid CreateDeal deal) {
        String dealId = idGenerator.generate();
        LocalDateTime createdAt = LocalDateTime.now();
        DealRow dealRow = new DealRow(dealId,
            deal.getTitle(),
            deal.getLink(),
            deal.getPrice(),
            deal.getCategory(),
            deal.getBrand(),
            createdAt);
        dynamoRepository.putItem(dealRow, builder -> builder.conditionExpression("attribute_not_exists(pk)"));
    }
}
