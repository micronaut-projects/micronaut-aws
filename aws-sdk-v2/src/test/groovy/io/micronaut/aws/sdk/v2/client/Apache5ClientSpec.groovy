package io.micronaut.aws.sdk.v2.client

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import io.micronaut.aws.sdk.v2.client.apache5.Apache5ClientConfiguration
import io.micronaut.aws.sdk.v2.client.apache5.Apache5ClientFactory
import io.micronaut.context.annotation.BootstrapContextCompatible
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.apache5.Apache5HttpClient

class Apache5ClientSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.apache-client.max-connections': 123,
                'aws.apache-client.proxy.username': 'username'
        ]
    }

    void "the apache 5 client is the elected sync client and is configured via aws.apache-client"() {
        when:
        Apache5HttpClient client = applicationContext.getBean(SdkHttpClient) as Apache5HttpClient

        then:
        client.resolvedOptions.get(SdkHttpConfigurationOption.MAX_CONNECTIONS) == 123
        client.requestConfig.proxyConfiguration().username() == 'username'
    }

    void "the apache 5 configuration and factory are BootstrapContextCompatible"() {
        expect:
        applicationContext.getBeanDefinition(Apache5ClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
        applicationContext.getBeanDefinition(Apache5ClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }
}
