package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.uri.UriBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import io.micronaut.http.annotation.Status;
import java.util.Optional;

@Requires(property = "spec.name", value = "EcommerceTest")
@Controller("/customers")
class CustomerController {
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
    HttpResponse saveOrder(@PathVariable String username, @Body CreateOrder order) {
        String orderId = orderRepository.save(username, order);
        return HttpResponse.created(UriBuilder.of("/customers").path(username).path("orders").path(orderId).build());
    }

    @Get("/{username}/orders/{orderId}")
    @Status(HttpStatus.OK)
    public Optional<Order> findOrder(@PathVariable String username, @PathVariable String orderId) {
        return orderRepository.findByUsernameAndOrderId(username, orderId);
    }

    @Put("/{username}/orders/{orderId}/status")
    @Status(HttpStatus.NO_CONTENT)
    void updateStatusForOrder(@PathVariable String username, @PathVariable String orderId, @Body("status") io.micronaut.aws.dynamodb.ecommerce.Status status) {
        orderRepository.updateStatusUsernameAndOrderId(username, orderId, status);

    }

    @Get("/{username}")
    Optional<Customer> find(@PathVariable String username) {
        return customerRepository.findByUsername(username);
    }
}
