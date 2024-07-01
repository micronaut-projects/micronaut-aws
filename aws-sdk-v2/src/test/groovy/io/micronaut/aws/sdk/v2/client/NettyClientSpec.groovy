package io.micronaut.aws.sdk.v2.client

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient

class NettyClientSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.netty-client.max-concurrency': 123
        ]
    }

    void "netty client should not be configured with empty proxy configuration"() {
        when:
        NettyNioAsyncHttpClient client = applicationContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
        client.pools.proxyConfiguration
    }

}
