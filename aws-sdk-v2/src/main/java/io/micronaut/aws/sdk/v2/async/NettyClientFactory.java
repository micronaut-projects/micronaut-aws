package io.micronaut.aws.sdk.v2.async;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

import javax.inject.Singleton;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */

@Factory
public class NettyClientFactory {

    @Singleton
    @BootstrapContextCompatible
    public SdkAsyncHttpClient nettyClient(NettyClientConfiguration configuration) {
        return configuration.builder.build();
    }
}
