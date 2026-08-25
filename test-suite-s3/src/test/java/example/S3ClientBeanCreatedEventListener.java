package example;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.floci.testcontainers.FlociContainer;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Singleton
public class S3ClientBeanCreatedEventListener implements BeanCreatedEventListener<S3ClientBuilder> {

    @Override
    public S3ClientBuilder onCreated(BeanCreatedEvent<S3ClientBuilder> event) {
        FlociContainer floci = Floci.getContainer();
        S3ClientBuilder builder = event.getBean();
        builder.endpointOverride(Floci.endpoint(floci))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(floci.getAccessKey(), floci.getSecretKey())
                )
            )
            .region(Region.of(floci.getRegion()))
            .forcePathStyle(true);
        return builder;
    }
}
