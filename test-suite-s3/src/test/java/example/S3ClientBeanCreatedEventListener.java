package example;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import org.testcontainers.localstack.LocalStackContainer;
import io.micronaut.localstack.testcontainers.Localstack;

@Singleton
public class S3ClientBeanCreatedEventListener implements BeanCreatedEventListener<S3ClientBuilder> {

    @Override
    public S3ClientBuilder onCreated(BeanCreatedEvent<S3ClientBuilder> event) {
        LocalStackContainer localstack = Localstack.getLocalStackContainer("s3");
        S3ClientBuilder builder = event.getBean();
        builder.endpointOverride(localstack.getEndpoint())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                )
            )
            .region(Region.of(localstack.getRegion()));
        return builder;
    }
}
