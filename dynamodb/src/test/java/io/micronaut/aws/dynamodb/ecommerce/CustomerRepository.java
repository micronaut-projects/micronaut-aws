package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.DynamoDbConversionService;
import io.micronaut.aws.dynamodb.DynamoRepository;
import io.micronaut.aws.dynamodb.ecommerce.items.CustomerEmailRow;
import io.micronaut.aws.dynamodb.ecommerce.items.CustomerRow;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Put;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Requires(property = "spec.name", value = "EcommerceTest")
@Singleton
public class CustomerRepository {

    private final DynamoRepository dynamoRepository;
    private final DynamoDbConversionService dynamoDbConversionService;

    CustomerRepository(DynamoRepository dynamoRepository,
                       DynamoDbConversionService dynamoDbConversionService) {
        this.dynamoRepository = dynamoRepository;
        this.dynamoDbConversionService = dynamoDbConversionService;
    }


    void save(@NonNull @NotNull @Valid CreateCustomer customer) {
        CustomerEmail customerEmail = new CustomerEmail(customer.getUsername(), customer.getEmail());
        CustomerRow customerRow = new CustomerRow(CustomerRow.keyOf(customer.getUsername()), customerEmail.getUsername(), customer.getEmail(), customer.getName());
        CustomerEmailRow customerEmailRow = new CustomerEmailRow(CustomerEmailRow.keyOf(customer.getEmail()), customer.getEmail(), customer.getUsername());

        Map<String, AttributeValue> customerItemMap = dynamoDbConversionService.convert(customerRow);
        Consumer<Put.Builder> customerItemPutBuilder = builder -> builder.item(customerItemMap)
            .conditionExpression("attribute_not_exists(pk)");
        Map<String, AttributeValue> customerEmailItemMap = dynamoDbConversionService.convert(customerEmailRow);
        Consumer<Put.Builder> customerEmailItemPutBuilder = builder -> builder.item(customerEmailItemMap)
            .conditionExpression("attribute_not_exists(pk)");
        dynamoRepository.transactWriteItems(customerItemPutBuilder, customerEmailItemPutBuilder);
    }

    @NonNull
    public Optional<Customer> findByUsername(@NonNull String username) {
        return dynamoRepository.getItem(CustomerRow.keyOf(username), CustomerRow.class)
            .map(row -> new Customer(row.getUsername(), row.getEmail(), row.getName(), null));

    }
}
