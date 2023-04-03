package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.aws.dynamodb.DynamoRepository;
import io.micronaut.aws.dynamodb.ecommerce.items.CustomerRow;
import io.micronaut.aws.dynamodb.ecommerce.items.OrderItemRow;
import io.micronaut.aws.dynamodb.ecommerce.items.OrderRow;
import io.micronaut.aws.dynamodb.utils.AttributeValueUtils;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Requires(property = "spec.name", value = "EcommerceTest")
@Singleton
public class OrderRepository {
    public static final int SCALE = 2;
    private final DynamoRepository dynamoRepository;
    private final IdGenerator idGenerator;

    public OrderRepository(DynamoRepository dynamoRepository, IdGenerator idGenerator) {
        this.dynamoRepository = dynamoRepository;
        this.idGenerator = idGenerator;
    }

    public String save(@NonNull String username, @NonNull @Valid CreateOrder createOrder) {
        String orderId = idGenerator.generate();
        OrderRow orderDynamoDbItem = orderRowOf(username, orderId, createOrder);
        Map<String, AttributeValue> orderDynamoDbItemMap = dynamoRepository.getDynamoDbConversionService().convert(orderDynamoDbItem);

        List<Consumer<Put.Builder>> l = createOrder.getItems().stream()
            .map(item -> orderItemRowOf(orderId, item))
                .map(orderItemRow -> {
                    Consumer<Put.Builder> consumer = builder -> {
                        Map<String, AttributeValue> m = dynamoRepository.getDynamoDbConversionService().convert(orderItemRow);
                        builder.item(m);
                    };
                    return consumer;
                }).collect(Collectors.toList());
        l.add(builder -> builder.item(orderDynamoDbItemMap));
        TransactWriteItemsResponse transactWriteItemsResponse = dynamoRepository.transactWriteItems(l);
        return orderId;
    }

    @NonNull
    public Optional<Order> findByUsernameAndOrderId(@NonNull @NotBlank String username,
                                                    @NonNull @NotBlank String orderId) {

        QueryResponse rsp = dynamoRepository.query(builder -> builder.indexName(BootStrap.INDEX_GSI1)
            .keyConditionExpression("#gsi1Pk = :gsi1Pk")
            .expressionAttributeNames(Collections.singletonMap("#gsi1Pk", BootStrap.INDEX_GSI1_PK))
            .expressionAttributeValues(Collections.singletonMap(":gsi1Pk", AttributeValueUtils.s(OrderRow.gsi1Of(orderId).getPartionKey())))
            .scanIndexForward(false));
        if (!rsp.hasItems()) {
            return Optional.empty();
        }
        OrderRow orderRow = null;
        List<OrderItemRow> orderItemRows = new ArrayList<>();
        for (Map<String, AttributeValue> item : rsp.items()) {
            if (item.containsKey("type")) {
                String type = item.get("type").s();
                if (type != null) {
                    if (type.equals(OrderRow.class.getName())) {
                        orderRow = dynamoRepository.getDynamoDbConversionService().convert(item, OrderRow.class);

                    } else if (type.equals(OrderItemRow.class.getName())) {
                        orderItemRows.add(dynamoRepository.getDynamoDbConversionService().convert(item, OrderItemRow.class));
                    }
                }
            }
        }

        if (orderRow == null) {
            return Optional.empty();
        }
        return Optional.of(orderOfOrderRow(orderRow, orderItemRows));
    }

    public void updateStatusUsernameAndOrderId(@NonNull @NotBlank String username,
                                               @NonNull @NotBlank String orderId,
                                               @NonNull @NotNull Status status) {
        dynamoRepository.updateItem(OrderRow.keyOf(username, orderId), builder -> builder
            .conditionExpression("attribute_exists(pk)")
            .updateExpression("SET #status = :status")
            .expressionAttributeNames(Collections.singletonMap("#status", "status"))
            .expressionAttributeValues(Collections.singletonMap(":status", AttributeValueUtils.s(status.toString())))
            .returnValues(ReturnValue.ALL_NEW));
    }

    @NonNull
    private Order orderOfOrderRow(@NonNull OrderRow orderRow, @NonNull List<OrderItemRow> orderItemRows) {
        return new Order(orderRow.getUsername(),
            orderRow.getOrderId(),
            orderRow.getAddress(),
            orderRow.getCreatedAt(),
            orderRow.getStatus(),
            orderRow.getTotalAmount(),
            orderRow.getNumberItems(),
            orderItemRows.stream().map(this::orderItemOfOrderItemRow).collect(Collectors.toList()));
    }

    @NonNull
    private OrderItem orderItemOfOrderItemRow(@NonNull OrderItemRow orderItemRow) {
        return new OrderItem(orderItemRow.getOrderId(),
            orderItemRow.getItemId(), orderItemRow.getDescription(), orderItemRow.getPrice(), orderItemRow.getAmount());
    }

    @NonNull
    private OrderItemRow orderItemRowOf(@NonNull String orderId, @NonNull @Valid Item orderItem) {
        return new OrderItemRow(OrderItemRow.keyOf(orderId, orderItem.getItemId()),
            OrderItemRow.gsi1Of(orderId, orderItem.getItemId()),
            orderId,
            orderItem.getItemId(),
            orderItem.getDescription(),
            orderItem.getPrice(),
            orderItem.getAmount(),
            orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getAmount())).setScale(SCALE, RoundingMode.HALF_UP)
        );
    }

    @NonNull
    private OrderRow orderRowOf(@NonNull String username, @NonNull String orderId, @NonNull @Valid CreateOrder createOrder) {
        BigDecimal totalAmount = createOrder.getItems().stream().map(Item::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(SCALE, RoundingMode.HALF_UP);

        Integer numberItems = createOrder.getItems().size();
        return new OrderRow(OrderRow.keyOf(username, orderId),
            OrderRow.gsi1Of(orderId),
            username,
            orderId,
            createOrder.getAddress(),
            LocalDateTime.now(),
            Status.PLACED,
            totalAmount,
            numberItems
        );
    }

    @NonNull
    public List<Order> findAllByUsername(@NonNull @NotBlank String username) {
        QueryResponse queryResponse = dynamoRepository.query(builder -> builder.keyConditionExpression("#pk = :pk")
            .expressionAttributeNames(Collections.singletonMap("#pk", "pk"))
            .expressionAttributeValues(Collections.singletonMap(":pk", AttributeValueUtils.s(CustomerRow.keyOf(username).getPartionKey())))
            .scanIndexForward(false)
        );

        if (!queryResponse.hasItems()) {
            return Collections.emptyList();
        }
        List<Map<String, AttributeValue>> items = queryResponse.items();
        if (items == null) {
            return Collections.emptyList();
        }
        List<Order> result = new ArrayList<>();
        for(Map<String, AttributeValue> item : items) {

        }
        return result;

    }
}
