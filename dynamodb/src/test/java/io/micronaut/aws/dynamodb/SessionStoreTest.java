package io.micronaut.aws.dynamodb;

import io.micronaut.aws.dynamodb.conf.DynamoConfiguration;
import io.micronaut.aws.dynamodb.utils.AttributeValueUtils;
import io.micronaut.aws.dynamodb.utils.DynamoDbLocal;
import io.micronaut.aws.dynamodb.utils.TestDynamoRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpHeaderValues;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Property(name = "dynamodb.table-name", value = "sessions")
@Property(name = "spec.name", value = "SessionStoreTest")
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionStoreTest implements TestPropertyProvider {
    @Override
    public Map<String, String> getProperties() {
        return DynamoDbLocal.getProperties();
    }

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void sessionStore() {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpRequest<?> createSessionRequest = HttpRequest.POST("/sessions", new Credentials("myuser", "Password1"));
        HttpResponse<SessionResponse> sessionResponseHttpResponse = client.exchange(createSessionRequest, SessionResponse.class);
        assertEquals(HttpStatus.CREATED, sessionResponseHttpResponse.getStatus());
        SessionResponse sessionResponse = sessionResponseHttpResponse.body();
        assertNotNull(sessionResponse);
        assertNotNull(sessionResponse.getSessionId());

        Executable e = () -> client.exchange(HttpRequest.GET("/sessions").bearerAuth("foobar"));
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, e);
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());

        HttpResponse<User> userHttpResponse = client.exchange(HttpRequest.GET("/sessions").bearerAuth(sessionResponse.getSessionId()), User.class);
        assertEquals(HttpStatus.OK, userHttpResponse.getStatus());
        User user = userHttpResponse.body();
        assertNotNull(user);
        assertNotNull(user.getUsername());
        assertEquals("myuser", user.getUsername());

        HttpResponse<?> deleteHttpResponse = client.exchange(HttpRequest.DELETE("/sessions").bearerAuth(sessionResponse.getSessionId()));
        assertEquals(HttpStatus.NO_CONTENT, deleteHttpResponse.getStatus());

        e = () -> client.exchange(HttpRequest.GET("/sessions").bearerAuth(sessionResponse.getSessionId()));
        thrown = assertThrows(HttpClientResponseException.class, e);
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());
    }

    @Requires(property = "spec.name", value = "SessionStoreTest")
    @Singleton
    static class BootStrap extends TestDynamoRepository {
        public BootStrap(DynamoDbClient dynamoDbClient,
                                    DynamoConfiguration dynamoConfiguration) {
            super(dynamoDbClient, dynamoConfiguration);
        }

        @Override
        public CreateTableRequest createTableRequest(String tableName) {
            return CreateTableRequest.builder()
                .attributeDefinitions(attributeDefinition("sessionId", ScalarAttributeType.S), attributeDefinition("username", ScalarAttributeType.S))
                .keySchema(Collections.singletonList(keySchemaElement("sessionId", KeyType.HASH)))
                .globalSecondaryIndexes(gsi("userIndex", "username", ProjectionType.KEYS_ONLY))
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .tableName(tableName)
                .build();
        }
    }

    @Requires(property = "spec.name", value = "SessionStoreTest")
    @Singleton
    static class SessionRepository {
        private static final Logger LOG = LoggerFactory.getLogger(SessionRepository.class);
        private final SessionGenerator sessionGenerator;
        private final DynamoDbConversionService dynamoDbConversionService;
        private final DynamoRepository dynamoRepository;

        SessionRepository(SessionGenerator sessionGenerator,
                          DynamoDbConversionService dynamoDbConversionService,
                          DynamoRepository dynamoRepository) {
            this.sessionGenerator = sessionGenerator;
            this.dynamoDbConversionService = dynamoDbConversionService;
            this.dynamoRepository = dynamoRepository;
        }

        @NonNull
        Optional<String> save(@NonNull @NotBlank String username) {
            String sessionId = sessionGenerator.generate();
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime expiresAt = createdAt.plusWeeks(1);
            ZoneId zoneId = ZoneId.systemDefault();
            long ttl = expiresAt.atZone(zoneId).toEpochSecond();
            SessionItem sessionItem = new SessionItem(sessionId, username, createdAt, expiresAt, ttl);
            Map<String, AttributeValue> item = dynamoDbConversionService.convert(sessionItem);
            try {
                dynamoRepository.putItem(item, builder -> builder.conditionExpression("attribute_not_exists(sessionId)").build());
                return Optional.of(sessionId);
            } catch (ConditionalCheckFailedException e) {
                LOG.warn("Holy moley -- a UUID collision!");
            }
            return Optional.empty();
        }

        @NonNull
        public Optional<String> findUsernameBySessionId(@NonNull @NotBlank String sessionId) {
            return dynamoRepository.getItem(Collections.singletonMap("sessionId", AttributeValueUtils.s(sessionId)), User.class)
                .map(User::getUsername);
        }

        public void deleteSessionsByUsername(@NonNull @NotBlank String username) {
            QueryRequest queryRequest = dynamoRepository.queryRequestBuilder()
                .indexName("userIndex")
                .keyConditionExpression("#username = :username")
                .expressionAttributeNames(Collections.singletonMap("#username", "username"))
                .expressionAttributeValues(Collections.singletonMap(":username", AttributeValueUtils.s(username)))
                .build();
            QueryResponse response = dynamoRepository.query(queryRequest);
            List<WriteRequest> deletes = response.items()
                .stream()
                .filter(item -> item.containsKey("sessionId"))
                .map(item -> DeleteRequest.builder()
                    .key(Collections.singletonMap("sessionId", item.get("sessionId")))
                    .build())
                .map(deleteRequest -> WriteRequest.builder().deleteRequest(deleteRequest).build())
                .toList();
            BatchWriteItemResponse batchWriteItemResponse = dynamoRepository.getDynamoDbClient()
                .batchWriteItem(BatchWriteItemRequest.builder()
                    .requestItems(Collections.singletonMap("sessions", deletes))
                    .build());
        }
    }

    @Introspected
    static class SessionItem {
        @NonNull
        @NotBlank
        private final String sessionId;
        @NonNull
        @NotBlank
        private final String username;

        @NonNull
        @NotNull
        private final LocalDateTime createdAt;

        @NonNull
        @NotNull
        private final LocalDateTime expiresAt;

        private final long ttl;

        SessionItem(String sessionId, String username, LocalDateTime createdAt, LocalDateTime expiresAt, long ttl) {
            this.sessionId = sessionId;
            this.username = username;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.ttl = ttl;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUsername() {
            return username;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getExpiresAt() {
            return expiresAt;
        }

        public long getTtl() {
            return ttl;
        }
    }

    @Requires(property = "spec.name", value = "SessionStoreTest")
    @Controller("/sessions")
    static class SessionController {

        private final SessionRepository sessionRepository;

        SessionController(SessionRepository sessionRepository) {
            this.sessionRepository = sessionRepository;
        }

        @ExecuteOn(TaskExecutors.IO)
        @Post
        HttpResponse<?> save(@NonNull @NotNull @Valid @Body Credentials credentials) {
            return sessionRepository.save(credentials.getUsername())
                .map(sessionId -> HttpResponse.created(new SessionResponse(sessionId)))
                .orElseGet(() -> {
                    MutableHttpResponse response = HttpResponse.serverError("could not create session token");
                    return response;
                });
        }

        @ExecuteOn(TaskExecutors.IO)
        @Get
        HttpResponse<?> get(@NonNull @Header(HttpHeaders.AUTHORIZATION) String authorization) {
            return sessionRepository.findUsernameBySessionId(sessionIdOfAuthorizationHeader(authorization))
                .map(User::new)
                .map(HttpResponse::ok)
                .orElseGet(HttpResponse::unauthorized);
        }

        @ExecuteOn(TaskExecutors.IO)
        @Delete
        @Status(HttpStatus.NO_CONTENT)
        void delete(@NonNull @Header(HttpHeaders.AUTHORIZATION) String authorization) {
            sessionRepository.findUsernameBySessionId(sessionIdOfAuthorizationHeader(authorization))
                .ifPresent(username -> sessionRepository.deleteSessionsByUsername(username));
        }

        @NonNull
        private String sessionIdOfAuthorizationHeader(@NonNull String authorization) {
            return authorization.startsWith(HttpHeaderValues.AUTHORIZATION_PREFIX_BEARER + " ") ?
                authorization.substring((HttpHeaderValues.AUTHORIZATION_PREFIX_BEARER + " ").length()) :
                authorization;
        }
    }

    @Serdeable
    static class User {
        @NonNull
        @NotBlank
        private final String username;

        public User(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }

    @Requires(property = "spec.name", value = "SessionStoreTest")
    @Singleton
    static class SessionGenerator {
        String generate() {
            return UUID.randomUUID().toString();
        }
    }

    @Serdeable
    static class SessionResponse {
        private final String sessionId;
        public SessionResponse(String sessionId) {
            this.sessionId = sessionId;
        }
        public String getSessionId() {
            return sessionId;
        }
    }

    @Serdeable
    static class Credentials {
        private final String username;
        private final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }


}
