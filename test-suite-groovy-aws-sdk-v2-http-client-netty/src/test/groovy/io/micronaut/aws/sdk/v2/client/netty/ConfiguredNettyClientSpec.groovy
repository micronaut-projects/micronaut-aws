package io.micronaut.aws.sdk.v2.client.netty

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import spock.lang.Specification

@Property(name = "aws.netty-client.max-concurrency", value = "123")
@Property(name = "aws.netty-client.proxy.host", value = "localhost")
@MicronautTest(startApplication = false)
class ConfiguredNettyClientSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "netty client can be configured"() {
        when:
        NettyNioAsyncHttpClient client = beanContext.getBean(SdkAsyncHttpClient) as NettyNioAsyncHttpClient

        then:
        client.configuration().maxConnections() == 123
        client.pools.proxyConfiguration.host == 'localhost'
    }
}
