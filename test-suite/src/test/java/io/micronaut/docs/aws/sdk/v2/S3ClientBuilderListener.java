package io.micronaut.docs.aws.sdk.v2;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Requires(property = "spec.name", value = "S3ClientBuilderTest")
//tag::listener[]
@Singleton
public class S3ClientBuilderListener implements BeanCreatedEventListener<S3ClientBuilder> {

    @Override
    public S3ClientBuilder onCreated(BeanCreatedEvent<S3ClientBuilder> event) {
        S3ClientBuilder builder = event.getBean();
        builder.overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryMode.LEGACY).build());

        return builder;
    }
}
//end::listener[]
