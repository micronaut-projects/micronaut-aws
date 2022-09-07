package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClientBuilder
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClientBuilder

class ServiceDiscoveryClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return ServiceDiscoveryClient.SERVICE_NAME
    }

    void "it can configure a service discovery client"() {
        when:
        ServiceDiscoveryClient client = applicationContext.getBean(ServiceDiscoveryClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async service discovery client"() {
        when:
        ServiceDiscoveryAsyncClient client = applicationContext.getBean(ServiceDiscoveryAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async service discovery client Builder"() {
        expect:
        applicationContext.getBean(ServiceDiscoveryAsyncClientBuilder)
    }

    void "it can configure a service discovery client Builder"() {
        expect:
        applicationContext.getBean(ServiceDiscoveryClientBuilder)
    }

    void "it can override the endpoint"() {
        when:
        ServiceDiscoveryClient client = applicationContext.getBean(ServiceDiscoveryClient)
        ServiceDiscoveryAsyncClient asyncClient = applicationContext.getBean(ServiceDiscoveryAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
