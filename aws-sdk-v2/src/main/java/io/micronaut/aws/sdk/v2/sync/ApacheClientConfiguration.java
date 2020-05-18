package io.micronaut.aws.sdk.v2.sync;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@ConfigurationProperties("apache-client")
@BootstrapContextCompatible
public class ApacheClientConfiguration extends AWSConfiguration {

    @ConfigurationBuilder(prefixes = {""})
    ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

}
