package io.micronaut.aws.sdk.v2.service;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Pavol Gressa
 * @since 2.5
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SdkClients {
    SdkClient[] value();
}
