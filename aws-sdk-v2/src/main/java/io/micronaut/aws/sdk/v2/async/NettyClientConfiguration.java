package io.micronaut.aws.sdk.v2.async;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@ConfigurationProperties("netty-client")
@BootstrapContextCompatible
public class NettyClientConfiguration extends AWSConfiguration {

    @ConfigurationBuilder(prefixes = {""})
    NettyNioAsyncHttpClient.Builder builder = NettyNioAsyncHttpClient.builder();

}
