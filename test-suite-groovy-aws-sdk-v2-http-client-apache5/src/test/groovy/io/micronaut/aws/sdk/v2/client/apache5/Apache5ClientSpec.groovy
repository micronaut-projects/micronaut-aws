package io.micronaut.aws.sdk.v2.client.apache5

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.apache5.Apache5HttpClient
import spock.lang.Specification

@Property(name = "aws.apache-client.max-connections", value = "123")
@Property(name = "aws.apache-client.proxy.username", value = "username")
@MicronautTest(startApplication = false)
class Apache5ClientSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "the apache 5 client is the elected sync client and is configured via aws.apache-client"() {
        when:
        Apache5HttpClient client = beanContext.getBean(SdkHttpClient) as Apache5HttpClient

        then:
        client.resolvedOptions.get(SdkHttpConfigurationOption.MAX_CONNECTIONS) == 123
        client.requestConfig.proxyConfiguration().username() == 'username'
    }

    void "the apache 5 configuration and factory are BootstrapContextCompatible"() {
        expect:
        beanContext.getBeanDefinition(Apache5ClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
        beanContext.getBeanDefinition(Apache5ClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }
}
