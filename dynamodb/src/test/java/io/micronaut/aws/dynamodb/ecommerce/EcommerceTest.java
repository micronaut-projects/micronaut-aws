package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.utils.DynamoDbLocal;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

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

        findCustomer(client, path, username, email, name);

        String location = saveOrder(client, path, username);
        String orderId = findOrder(client, location);

        updateOrderStatus(client, path, username, orderId, Status.CANCELLED);
        HttpResponse<Order> orderResponse = client.exchange(location, Order.class);
        assertEquals(HttpStatus.OK, orderResponse.getStatus());
        Order order = orderResponse.body();
        assertNotNull(order);
        assertEquals(Status.CANCELLED, order.getStatus());
    }

    private void findCustomer(BlockingHttpClient client, String path, String username, String email, String name) {
        HttpRequest<?> findCustomerRequest = HttpRequest.GET(UriBuilder.of(path).path(username).build());
        HttpResponse<Customer> findCustomerResponse = client.exchange(findCustomerRequest, Customer.class);
        assertEquals(HttpStatus.OK, findCustomerResponse.getStatus());
        Customer customer = findCustomerResponse.body();
        assertNotNull(customer);
        assertEquals(username, customer.getUsername());
        assertEquals(email, customer.getEmail());
        assertEquals(name, customer.getName());
        assertNull(customer.getAddresses());
    }

    private String saveOrder(BlockingHttpClient client, String path, String username) {
        Map orderBody = Map.of("address", Map.of("streetAddress", "123 1st Street", "postalCode", "10001", "country", "USA"),
            "items", Collections.singletonList(Map.of("itemId", "1d45", "description", "Air Force 1s", "price", 15.99, "amount", 1)));

        URI uri = UriBuilder.of(path).path(username).path("orders").build();
        HttpRequest<?> saveOrderRequest = HttpRequest.POST(uri, orderBody);
        HttpResponse<?> saveOrderResponse = client.exchange(saveOrderRequest);
        assertEquals(HttpStatus.CREATED, saveOrderResponse.getStatus());
        String location = saveOrderResponse.getHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(location);
        return location;
    }

    private String findOrder(BlockingHttpClient client, String location) {
        HttpResponse<Order> orderResponse = client.exchange(location, Order.class);
        assertEquals(HttpStatus.OK, orderResponse.getStatus());
        Order order = orderResponse.body();
        assertNotNull(order);
        assertEquals(Status.PLACED, order.getStatus());
        assertNotNull(order.getAddress());
        assertEquals("123 1st Street", order.getAddress().getStreetAddress());
        assertEquals("USA", order.getAddress().getCountry());
        assertEquals("10001", order.getAddress().getPostalCode());
        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size());
        assertNotNull(order.getOrderId());
        assertEquals("Air Force 1s", order.getItems().get(0).getDescription());
        assertEquals("1d45", order.getItems().get(0).getItemId());
        assertEquals(1, order.getItems().get(0).getAmount());
        assertEquals(new BigDecimal("15.99"), order.getItems().get(0).getPrice());
        return order.getOrderId();
    }

    private void updateOrderStatus(BlockingHttpClient client, String path, String username, String orderId, Status status) {
        URI updateStatusUri = UriBuilder.of(path).path(username).path("orders").path(orderId).path("status").build();
        HttpResponse<?> updateStatusResponse = client.exchange(HttpRequest.PUT(updateStatusUri, Map.of("status", status)));
        assertEquals(HttpStatus.NO_CONTENT, updateStatusResponse.getStatus());
    }
}
