package io.micronaut.aws.sdk.v2.client

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import spock.lang.Specification

class NettyClientSpec extends Specification {

    void "netty client can be configured"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
               [
                       'aws.netty-client.max-concurrency': 123,
                       'aws.netty-client.proxy.host': 'localhost'
               ]
        )

        when:
        NettyNioAsyncHttpClient client = applicationContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
        client.pools.proxyConfiguration.host == 'localhost'
    }

    void "netty client should not be configured with empty proxy configuration"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                [
                        'aws.netty-client.max-concurrency': 123
                ]
        )

        when:
        NettyNioAsyncHttpClient client = applicationContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
        client.pools.proxyConfiguration == null
    }

}
