package io.micronaut.aws.sdk.v2;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import jakarta.inject.Singleton;

@Requires(property = "spec.name", value = "S3ClientSpec.builders")
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