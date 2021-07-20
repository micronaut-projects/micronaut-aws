/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.discovery.aws.servicediscovery

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.discovery.CompositeDiscoveryClient
import io.micronaut.discovery.DiscoveryClient
import io.micronaut.discovery.ServiceInstance
import io.micronaut.discovery.aws.servicediscovery.client.AwsServiceDiscoveryClient
import io.micronaut.discovery.aws.servicediscovery.registration.AwsServiceDiscoveryRegistrationClient
import io.micronaut.discovery.client.registration.DiscoveryServiceAutoRegistration
import io.micronaut.discovery.cloud.ComputeInstanceMetadata
import io.micronaut.discovery.cloud.ComputeInstanceMetadataResolver
import io.micronaut.discovery.cloud.NetworkInterface
import io.micronaut.discovery.cloud.aws.AmazonComputeInstanceMetadataResolver
import io.micronaut.discovery.cloud.aws.AmazonEC2InstanceMetadata
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import io.reactivex.Flowable
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient
import software.amazon.awssdk.services.servicediscovery.model.GetOperationResponse
import software.amazon.awssdk.services.servicediscovery.model.InstanceSummary
import software.amazon.awssdk.services.servicediscovery.model.ListInstancesResponse
import software.amazon.awssdk.services.servicediscovery.model.ListServicesResponse
import software.amazon.awssdk.services.servicediscovery.model.Operation
import software.amazon.awssdk.services.servicediscovery.model.OperationStatus
import software.amazon.awssdk.services.servicediscovery.model.RegisterInstanceRequest
import software.amazon.awssdk.services.servicediscovery.model.RegisterInstanceResponse
import software.amazon.awssdk.services.servicediscovery.model.Service
import software.amazon.awssdk.services.servicediscovery.model.ServiceSummary
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.CompletableFuture

/**
 * @author Rvanderwerf @
 * @author Denis Stepanov
 * @since 1.0
 */

@MicronautTest(environments = Environment.AMAZON_EC2)
class AwsServiceDiscoverySpec extends Specification implements TestPropertyProvider {

    @Inject
    @Shared
    ApplicationContext applicationContext

    @Shared
    AwsServiceDiscoveryRegistrationClient client
    @Shared
    DiscoveryClient discoveryClient
    @Shared
    AwsServiceDiscoveryClient awsServiceDiscoveryClient
    @Shared
    ServiceDiscoveryClient serviceDiscoveryClient
    @Shared
    ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient
    @Shared
    String namespaceId
    @Shared
    String serviceId
    @Shared
    String createdInstanceId

    @Override
    Map<String, String> getProperties() {
        ["aws.service-discovery.registration.namespace"   : "vanderfox.net",
         "aws.service-discovery.registration.awsServiceId": "testId",
         "aws.service-discovery.enabled"                  : "true",
         "aws.service-discovery.registration.enabled"     : "true",
         "micronaut.application.name"                     : "testapp",
         "spec.name"                                      : getClass().simpleName]
    }

    @Bean
    @Singleton
    ServiceDiscoveryClient buildServiceDiscoveryClient() {
        Mock(ServiceDiscoveryClient) {
            getOperation(_) >> GetOperationResponse.builder()
                    .operation(Operation.builder().id("123456").status(OperationStatus.SUCCESS).build())
                    .build()
        }
    }

    @Bean
    @Singleton
    ServiceDiscoveryAsyncClient buildServiceDiscoveryAsyncClient() {
        Mock(ServiceDiscoveryAsyncClient) {
            registerInstance(_) >> CompletableFuture.completedFuture(
                    RegisterInstanceResponse.builder().operationId("abc123").build()
            )
        }
    }

    def setupSpec() {

        client = applicationContext.getBean(AwsServiceDiscoveryRegistrationClient)
        discoveryClient = applicationContext.getBean(DiscoveryClient)
        awsServiceDiscoveryClient = applicationContext.getBean(AwsServiceDiscoveryClient)
        serviceDiscoveryClient = applicationContext.getBean(ServiceDiscoveryClient)
        serviceDiscoveryAsyncClient = applicationContext.getBean(ServiceDiscoveryAsyncClient)

        namespaceId = "asdb123"
        serviceId = "123abcdf"

        createdInstanceId = "i-12123321"

        awsServiceDiscoveryClient.awsServiceDiscoveryClientConfiguration.awsServiceId = serviceId
        awsServiceDiscoveryClient.awsServiceDiscoveryClientConfiguration.namespaceId = namespaceId
    }

    void "test is a discovery client"() {
        expect:
            discoveryClient instanceof CompositeDiscoveryClient
            client instanceof DiscoveryServiceAutoRegistration
    }

    void "test register and de-register instance"() {

        given:
            createdInstanceId = "i-12123321"
            1 * serviceDiscoveryAsyncClient.registerInstance(_) >> CompletableFuture.completedFuture(
                    RegisterInstanceResponse.builder().operationId("abc123").build()
            )
            1 * serviceDiscoveryAsyncClient.listServices(_) >> CompletableFuture.completedFuture(
                    ListServicesResponse.builder()
                            .services(ServiceSummary.builder()
                                    .instanceCount(1)
                                    .name("123456")
                                    .id(serviceId)
                                    .build())
                            .build()
            )
            1 * serviceDiscoveryAsyncClient.listInstances(_) >> CompletableFuture.completedFuture(
                    ListInstancesResponse.builder()
                            .instances(InstanceSummary.builder()
                                    .id(createdInstanceId)
                                    .attributes(["URI": "/v1"])
                                    .build())
                            .build()
            )

        when:
            def instanceId = createdInstanceId
            def builder = ServiceInstance.builder("test", new URI("/v1")).instanceId(instanceId)
            ServiceInstance serviceInstance = builder.build()
            client.register(serviceInstance)

            List<String> serviceIds = Flowable.fromPublisher(discoveryClient.getServiceIds()).blockingFirst()
            assert serviceIds != null

            List<ServiceInstance> instances = Flowable.fromPublisher(discoveryClient.getInstances(serviceIds.get(0))).blockingFirst()

            instances.size() == 1
            instances != null
            serviceIds != null

        then:
            client.deregister(serviceInstance)
    }

    /**
     * these are excluded if you are running the integration test as you can't do both at once
     */
    @Replaces(AmazonComputeInstanceMetadataResolver)
    @Requires(property = 'spec.name', value = 'Route53AutoNamingClientUnitSpec')
    static class AmazonComputeInstanceMetadataResolverMock extends AmazonComputeInstanceMetadataResolver implements ComputeInstanceMetadataResolver {

        @Override
        Optional<ComputeInstanceMetadata> resolve(Environment environment) {
            AmazonEC2InstanceMetadata metadata = new AmazonEC2InstanceMetadata()
            String createdInstanceId = "i-12123321"
            // we will need to call our getInstance Details since we are not running this on a real aws server and trick the resolver for the test
            metadata.instanceId = createdInstanceId
            metadata.publicIpV4 = "10.0.0.2"
            metadata.privateIpV4 = "10.0.0.3"


            metadata.machineType = "t2.nano"
            metadata.localHostname = "i12123321.ec2.internal"


            NetworkInterface micronautNetworkInterface = new NetworkInterface()
            micronautNetworkInterface.ipv4 = "10.0.0.3"
            micronautNetworkInterface.network = "s-123123"
            micronautNetworkInterface.mac = "0a:0d:0c:3a"
            micronautNetworkInterface.name = "eth0"
            metadata.interfaces = [micronautNetworkInterface]
            metadata.metadata = new HashMap<String, String>();
            metadata.metadata.put("instanceId", createdInstanceId);
            metadata.metadata.put("machineType", "t2.nano");
            Optional.of(metadata)
        }
    }

}