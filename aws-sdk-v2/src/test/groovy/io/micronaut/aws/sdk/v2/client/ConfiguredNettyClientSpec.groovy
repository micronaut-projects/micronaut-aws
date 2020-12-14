package io.micronaut.aws.sdk.v2.client

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient

class ConfiguredNettyClientSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.netty-client.max-concurrency': 123,
                'aws.netty-client.proxy.host': 'localhost'
        ]
    }

    void "netty client can be configured"() {
        when:
        NettyNioAsyncHttpClient client = applicationContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
        client.pools.proxyConfiguration.host == 'localhost'
    }
}
