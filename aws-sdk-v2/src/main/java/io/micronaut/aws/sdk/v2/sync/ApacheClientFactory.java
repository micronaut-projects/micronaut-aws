package io.micronaut.aws.sdk.v2.sync;

import io.micronaut.context.annotation.Factory;
import software.amazon.awssdk.http.SdkHttpClient;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@Factory
public class ApacheClientFactory {

    public SdkHttpClient apacheClient(ApacheClientConfiguration configuration) {
        return configuration.builder.build();
    }
}
