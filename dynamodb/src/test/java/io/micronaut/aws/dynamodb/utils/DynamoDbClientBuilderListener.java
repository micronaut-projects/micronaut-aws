package io.micronaut.aws.dynamodb.utils;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.ConfigurationException;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Requires(property = "dynamodb-local.host")
@Requires(property = "dynamodb-local.port")
@Singleton
class DynamoDbClientBuilderListener
        implements BeanCreatedEventListener<DynamoDbClientBuilder> {
    private final URI endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;

    DynamoDbClientBuilderListener(@Value("${dynamodb-local.host}") String host,
                                  @Value("${dynamodb-local.port}") String port) {
        try {
            this.endpoint = new URI("http://" + host + ":" + port);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("dynamodb.endpoint not a valid URI");
        }
        this.accessKeyId = "fakeMyKeyId";
        this.secretAccessKey = "fakeSecretAccessKey";
    }

    @Override
    public DynamoDbClientBuilder onCreated(BeanCreatedEvent<DynamoDbClientBuilder> event) {
        return event.getBean().endpointOverride(endpoint)
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return accessKeyId;
                    }

                    @Override
                    public String secretAccessKey() {
                        return secretAccessKey;
                    }
                });
    }
}
