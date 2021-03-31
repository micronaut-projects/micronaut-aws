package io.micronaut.aws.sdk.v2.service;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Internal Annotation to trigger the creation of SDK clients.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@Repeatable(SdkClients.class)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SdkClient {
    Class<?> client();

    Class<?> clientBuilder();

    Class<?> asyncClient();

    Class<?> asyncClientBuilder();
}

