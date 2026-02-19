package example;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import org.testcontainers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import io.micronaut.localstack.testcontainers.Localstack;

@Singleton
public class S3AsyncClientBeanCreatedEventListener implements BeanCreatedEventListener<S3AsyncClientBuilder> {

    @Override
    public S3AsyncClientBuilder onCreated(BeanCreatedEvent<S3AsyncClientBuilder> event) {
        LocalStackContainer localstack = Localstack.getLocalStackContainer("s3");
        S3AsyncClientBuilder builder = event.getBean();
        builder.endpointOverride(localstack.getEndpoint())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                )
            ).region(Region.of(localstack.getRegion()));
        return builder;
    }
}
