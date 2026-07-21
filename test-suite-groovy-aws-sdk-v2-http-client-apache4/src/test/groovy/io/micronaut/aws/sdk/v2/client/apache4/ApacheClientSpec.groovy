package io.micronaut.aws.sdk.v2.client.apache4

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.apache.ApacheHttpClient
import spock.lang.Specification

@Property(name = "aws.apache-client.max-connections", value = "123")
@Property(name = "aws.apache-client.proxy.username", value = "username")
@MicronautTest(startApplication = false)
class ApacheClientSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "the apache 4 client is the elected sync client and is configured via aws.apache-client"() {
        when:
        ApacheHttpClient client = beanContext.getBean(SdkHttpClient) as ApacheHttpClient

        then:
        client.resolvedOptions.get(SdkHttpConfigurationOption.MAX_CONNECTIONS) == 123
        client.requestConfig.proxyConfiguration().username() == 'username'
    }
}
