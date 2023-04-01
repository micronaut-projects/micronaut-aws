package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.BaseItem;
import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.aws.dynamodb.DynamoDbConversionService;
import io.micronaut.aws.dynamodb.DynamoRepository;
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

    @NonNull
    private CompositeKey customerItemKey(@NonNull String username) {
        return new BaseItem("CUSTOMER#" + username, "CUSTOMER#" + username);
    }

    void save(@NonNull @NotNull @Valid CreateCustomer customer) {
        CustomerEmail customerEmail = new CustomerEmail(customer.getUsername(), customer.getEmail());
        CustomerItem customerItem = new CustomerItem(customerItemKey(customer.getUsername()), customerEmail.getUsername(), customer.getEmail(), customer.getName());
        CustomerEmailItem customerEmailItem = new CustomerEmailItem("CUSTOMEREMAIL#" + customerEmail.getEmail(), "CUSTOMEREMAIL#" + customerEmail.getEmail(), customer.getEmail(), customer.getUsername());

        Map<String, AttributeValue> customerItemMap = dynamoDbConversionService.convert(customerItem);
        Consumer<Put.Builder> customerItemPutBuilder = builder -> builder.item(customerItemMap)
            .conditionExpression("attribute_not_exists(pk)");
        Map<String, AttributeValue> customerEmailItemMap = dynamoDbConversionService.convert(customerEmailItem);
        Consumer<Put.Builder> customerEmailItemPutBuilder = builder -> builder.item(customerEmailItemMap)
            .conditionExpression("attribute_not_exists(pk)");
        dynamoRepository.transactWriteItems(customerItemPutBuilder, customerEmailItemPutBuilder);
    }

    @NonNull
    public Optional<Customer> findByUsername(@NonNull String username) {
        CompositeKey key = customerItemKey(username);
        return dynamoRepository.getItem(key, Customer.class);

    }
}
