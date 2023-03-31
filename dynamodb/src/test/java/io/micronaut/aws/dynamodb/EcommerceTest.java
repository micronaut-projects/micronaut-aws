package io.micronaut.aws.dynamodb;

import io.micronaut.core.annotation.Creator;
import io.micronaut.aws.dynamodb.conf.DynamoConfiguration;
import io.micronaut.aws.dynamodb.utils.DynamoDbLocal;
import io.micronaut.aws.dynamodb.utils.TestDynamoRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Property(name = "dynamodb.table-name", value = EcommerceTest.TABLE_NAME)
@Property(name = "spec.name", value = "EcommerceTest")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EcommerceTest implements TestPropertyProvider {

    public static final String TABLE_NAME = "ecommerce";
    @Override
    public Map<String, String> getProperties() {
        return DynamoDbLocal.getProperties();
    }

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void ecommerceTest() {
        String username = "alexdebrie";
        String path = "/customers";
        String email = "alexdebrie1@gmail.com";
        String name = "Alex DeBrie";
        BlockingHttpClient client = httpClient.toBlocking();
        Map body = Map.of("username", username, "email", email, "name", name);
        HttpRequest<?> saveCustomerRequest = HttpRequest.POST(path, body);
        HttpResponse<?> saveCustomerResponse = client.exchange(saveCustomerRequest);
        assertEquals(HttpStatus.OK, saveCustomerResponse.getStatus());

        HttpRequest<?> findCustomerRequest = HttpRequest.GET(UriBuilder.of(path).path(username).build());
        HttpResponse<Customer> findCustomerResponse = client.exchange(findCustomerRequest, Customer.class);
        assertEquals(HttpStatus.OK, findCustomerResponse.getStatus());
        Customer customer = findCustomerResponse.body();
        assertNotNull(customer);
        assertEquals(username, customer.getUsername());
        assertEquals(email, customer.getEmail());
        assertEquals(name, customer.getName());
        assertNull(customer.getAddresses());

        Map orderBody = Map.of("address", Map.of("streetAddress", "123 1st Street", "postalCode", "10001", "country", "USA"),
            "items", Collections.singletonList(Map.of("itemId", "1d45", "description", "Air Force 1s", "price", 15.99, "amount", 1)));

        URI uri = UriBuilder.of(path).path(username).path("orders").build();
        HttpRequest<?> saveOrderRequest = HttpRequest.POST(uri, orderBody);
        HttpResponse<?> saveOrderResponse = client.exchange(saveOrderRequest);
        assertEquals(HttpStatus.OK, saveOrderResponse.getStatus());
    }

    @Requires(property = "spec.name", value = "EcommerceTest")
    @Singleton
    static class BootStrap extends TestDynamoRepository {
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
                    attributeDefinition("gsi1pk", ScalarAttributeType.S),
                    attributeDefinition("gsi1sk", ScalarAttributeType.S)
                )
                .keySchema(Arrays.asList(keySchemaElement("pk", KeyType.HASH), keySchemaElement("sk", KeyType.RANGE)))
                .globalSecondaryIndexes(
                    gsi("gsi1", "gsi1pk", "gsi1sk"))
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .tableName(tableName)
                .build();
        }
    }

    @Requires(property = "spec.name", value = "EcommerceTest")
    @Controller("/customers")
    static class CustomerController {

        private final CustomerRepository customerRepository;
        private final OrderRepository orderRepository;
        CustomerController(CustomerRepository customerRepository,
                           OrderRepository orderRepository) {
            this.customerRepository = customerRepository;
            this.orderRepository = orderRepository;
        }

        @Post
        @Status(HttpStatus.OK)
        void save(@NonNull @NotNull @Valid CreateCustomer customer) {
            customerRepository.save(customer);
        }

        @Post("/{username}/orders")
        @Status(HttpStatus.OK)
        void saveOrder(@PathVariable String username, @Body CreateOrder order) {
            String foo = "";
            orderRepository.save(username, order);
        }


        @Get("/{username}")
        @Status(HttpStatus.OK)
        Optional<Customer> find(@PathVariable String username) {
            return customerRepository.findByUsername(username);
        }
    }


    @Requires(property = "spec.name", value = "EcommerceTest")
    @Singleton
    static class CustomerRepository {

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

    @Requires(property = "spec.name", value = "EcommerceTest")
    @Singleton
    static class OrderRepository {

        private final DynamoRepository dynamoRepository;

        OrderRepository(DynamoRepository dynamoRepository) {
            this.dynamoRepository = dynamoRepository;
        }

        void save(@NonNull String username, @NonNull @Valid CreateOrder createOrder) {
            //TODO

        }

        @NonNull
        CompositeKey orderKey(@NonNull String username, @NonNull String orderId) {
            return new BaseItem("CUSTOMER#" + username, "ORDER#" + orderId);
        }

        @NonNull
        CompositeKey orderGsi1(String orderId) {
            return new BaseItem("ORDER#" + orderId, "ORDER#" + orderId);
        }
    }

    @Introspected
    static class CustomerEmail {
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        CustomerEmail(String username, String email) {
            this.username = username;
            this.email = email;
        }

        @NonNull
        public String getUsername() {
            return username;
        }

        @NonNull
        public String getEmail() {
            return email;
        }
    }

    @Serdeable
    static class CreateOrder {
        @NotNull
        @NonNull
        @Valid
        private final Address address;


                @Size(min = 1)
                @NotNull
                @NonNull
                private final List<@Valid OrderItem> items;

                public CreateOrder(@NonNull Address address,
                                   @NonNull List<@Valid OrderItem> items) {
                    this.address = address;
                    this.items = items;
                }

        @NonNull
        public Address getAddress() {
            return address;
        }

        @NonNull
        public List<OrderItem> getItems() {
            return items;
        }
    }
    @Serdeable
    static class OrderItem {
        @NonNull
        @NotBlank
        private final String itemId;

        @NotBlank
        @NonNull
        private final String description;

        @NonNull
        @NotNull
        private final BigDecimal price;

        @NonNull
        @NotNull
        private final Integer amount;

        OrderItem(String itemId, String description, BigDecimal price, Integer amount) {
            this.itemId = itemId;
            this.description = description;
            this.price = price;
            this.amount = amount;
        }

        public String getItemId() {
            return itemId;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Integer getAmount() {
            return amount;
        }
    }

    @Serdeable
    static class Address {

        private final String streetAddress;
        private final String postalCode;
        private final String country;

        public Address(String streetAddress, String postalCode, String country) {
            this.streetAddress = streetAddress;
            this.postalCode = postalCode;
            this.country = country;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public String getCountry() {
            return country;
        }
    }

    @Serdeable
    static class CustomerEmailItem extends BaseItem {
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        public CustomerEmailItem(String pk, String sk, String username, String email) {
            super(pk, sk);
            this.username = username;
            this.email = email;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }

    @Serdeable
    static class CustomerItem extends BaseItem {

        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        @NonNull
        @NotBlank
        private final String name;

        @Creator
        public CustomerItem(String pk, String sk, String username, String email, String name) {
            super(pk, sk);
            this.username = username;
            this.email = email;
            this.name = name;
        }

        public CustomerItem(CompositeKey key, String username, String email, String name) {
            super(key.getPk(), key.getSk());
            this.username = username;
            this.email = email;
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }

    @Serdeable
    static class CreateCustomer {
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        @NonNull
        @NotBlank
        private final String name;


        CreateCustomer(String username, String email, String name) {
            this.username = username;
            this.email = email;
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }

    @Serdeable
    static class Customer {
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotBlank
        @Email
        private final String email;

        @NonNull
        @NotBlank
        private final String name;

        @Nullable
        private List<Address> addresses;

        public Customer(@NonNull String username,
                        @NonNull String email,
                        @NonNull String name,
                        @Nullable List<Address> addresses) {
            this.username = username;
            this.email = email;
            this.name = name;
            this.addresses = addresses;
        }

        @NonNull
        public String getUsername() {
            return username;
        }

        @NonNull
        public String getEmail() {
            return email;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @Nullable
        public List<Address> getAddresses() {
            return addresses;
        }
    }
}
