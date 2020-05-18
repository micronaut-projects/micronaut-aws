package io.micronaut.aws.sdk.v2

import groovy.transform.NotYetImplemented
import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import spock.lang.Issue
import spock.lang.Specification

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
class NettyClientSpec extends Specification {

    void "netty client can be configured"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
               ['aws.netty-client.max-concurrency': 123]
        )

        when:
        NettyNioAsyncHttpClient client = applicationContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
    }

}
