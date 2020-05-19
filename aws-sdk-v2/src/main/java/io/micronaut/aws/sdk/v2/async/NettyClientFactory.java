package io.micronaut.aws.sdk.v2.async;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */

@Factory
public class NettyClientFactory {

    @Bean(preDestroy = "close")
    @BootstrapContextCompatible
    public SdkAsyncHttpClient nettyClient(NettyClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }
}
