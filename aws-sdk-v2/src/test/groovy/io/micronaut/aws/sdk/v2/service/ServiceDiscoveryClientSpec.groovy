package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient

class ServiceDiscoveryClientSpec extends ServiceClientSpec<ServiceDiscoveryClient, ServiceDiscoveryAsyncClient> {
    @Override
    protected String serviceName() {
        return ServiceDiscoveryClient.SERVICE_NAME
    }

    @Override
    protected ServiceDiscoveryClient getClient() {
        applicationContext.getBean(ServiceDiscoveryClient)
    }

    protected ServiceDiscoveryAsyncClient getAsyncClient() {
        applicationContext.getBean(ServiceDiscoveryAsyncClient)
    }
}
