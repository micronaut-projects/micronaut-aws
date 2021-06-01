package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClientBuilder;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClientBuilder;

class ServiceDiscoveryClientSpec extends ApplicationContextSpecification {

    void "it can configure a service discovery client"() {
        when:
        ServiceDiscoveryClient client = applicationContext.getBean(ServiceDiscoveryClient)

        then:
        client.serviceName() == ServiceDiscoveryClient.SERVICE_NAME
    }

    void "it can configure an async service discovery client"() {
        when:
        ServiceDiscoveryAsyncClient client = applicationContext.getBean(ServiceDiscoveryAsyncClient)

        then:
        client.serviceName() == ServiceDiscoveryClient.SERVICE_NAME
    }

    void "it can configure an async service discovery client Builder"() {
        expect:
        applicationContext.getBean(ServiceDiscoveryAsyncClientBuilder)
    }

    void "it can configure a service discovery client Builder"() {
        expect:
        applicationContext.getBean(ServiceDiscoveryClientBuilder)
    }
}
