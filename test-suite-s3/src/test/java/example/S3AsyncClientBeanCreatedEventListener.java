package example;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.floci.testcontainers.FlociContainer;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;

@Singleton
public class S3AsyncClientBeanCreatedEventListener implements BeanCreatedEventListener<S3AsyncClientBuilder> {

    @Override
    public S3AsyncClientBuilder onCreated(BeanCreatedEvent<S3AsyncClientBuilder> event) {
        FlociContainer floci = Floci.getContainer();
        S3AsyncClientBuilder builder = event.getBean();
        builder.endpointOverride(Floci.endpoint(floci))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())
                )
            ).region(Region.of(floci.getRegion()))
            .forcePathStyle(true);
        return builder;
    }
}
