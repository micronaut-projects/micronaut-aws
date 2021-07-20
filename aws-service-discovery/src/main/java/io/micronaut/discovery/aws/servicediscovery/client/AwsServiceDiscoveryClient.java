/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.discovery.aws.servicediscovery.client;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryClientConfiguration;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryConfiguration;
import io.micronaut.discovery.aws.servicediscovery.registration.EC2ServiceInstance;
import org.reactivestreams.Publisher;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.model.InstanceSummary;
import software.amazon.awssdk.services.servicediscovery.model.ListInstancesRequest;
import software.amazon.awssdk.services.servicediscovery.model.ListInstancesResponse;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesRequest;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesResponse;
import software.amazon.awssdk.services.servicediscovery.model.ServiceFilter;
import software.amazon.awssdk.services.servicediscovery.model.ServiceFilterName;
import software.amazon.awssdk.services.servicediscovery.model.ServiceSummary;

import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the {@link DiscoveryClient} interface for AWS Route53.
 *
 * @author Rvanderwerf
 * @author graemerocher
 * @since 1.0
 */
@Internal
@Singleton
@Requires(property = AwsServiceDiscoveryConfiguration.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Requires(classes = {ServiceDiscoveryAsyncClient.class})
@Requires(env = Environment.AMAZON_EC2)
@Requires(beans = AwsServiceDiscoveryConfiguration.class)
public class AwsServiceDiscoveryClient implements DiscoveryClient {

    private final ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient;
    private final AwsServiceDiscoveryClientConfiguration awsServiceDiscoveryClientConfiguration;

    /**
     * Default constructor.
     *
     * @param awsServiceDiscoveryClientConfiguration The discovery configuration
     * @param serviceDiscoveryAsyncClient            The AWS serviceDiscoveryAsyncClient
     */
    public AwsServiceDiscoveryClient(AwsServiceDiscoveryClientConfiguration awsServiceDiscoveryClientConfiguration,
                                     ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient) {
        this.awsServiceDiscoveryClientConfiguration = awsServiceDiscoveryClientConfiguration;
        this.serviceDiscoveryAsyncClient = serviceDiscoveryAsyncClient;
    }

    /**
     * The description.
     */
    @Override
    public String getDescription() {
        return "Aws Service Discovery Client";
    }

    /**
     * transforms an aws result into a list of service instances.
     *
     * @param instancesResult instance result list of a service from aws service discovery
     * @return serviceInstance list that micronaut wants
     */
    private List<ServiceInstance> convertInstancesResultToServiceInstances(ListInstancesResponse instancesResult) {
        try {
            List<ServiceInstance> serviceInstances = new ArrayList<>(instancesResult.instances().size());
            for (InstanceSummary instanceSummary : instancesResult.instances()) {
                String uri = "http://" + instanceSummary.attributes().get("URI");
                serviceInstances.add(new EC2ServiceInstance(instanceSummary.id(), new URI(uri)).metadata(instanceSummary.attributes()).build());
            }
            return serviceInstances;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a list of instances registered with Route53 given a service ID.
     *
     * @param serviceId The service id
     * @return list of serviceInstances usable by MN.
     */
    @Override
    public Publisher<List<ServiceInstance>> getInstances(String serviceId) {
        if (serviceId == null) {
            serviceId = awsServiceDiscoveryClientConfiguration.getAwsServiceId();  // we can default to the config file
        }
        return Publishers.fromCompletableFuture(
                serviceDiscoveryAsyncClient.listInstances(ListInstancesRequest.builder().serviceId(serviceId).build())
                        .thenApply(this::convertInstancesResultToServiceInstances)
        );
    }

    /**
     * Gets a list of service IDs from AWS for a given namespace.
     *
     * @return publisher list of the service IDs in string format
     */
    @Override
    public Publisher<List<String>> getServiceIds() {
        return Publishers.fromCompletableFuture(
                serviceDiscoveryAsyncClient.listServices(
                        ListServicesRequest.builder()
                                .filters(
                                        ServiceFilter.builder()
                                                .name(ServiceFilterName.NAMESPACE_ID)
                                                .values(awsServiceDiscoveryClientConfiguration.getNamespaceId())
                                                .build()
                                ).build()
                ).thenApply(this::convertServiceIds)
        );
    }

    /**
     * Close down AWS Client on shutdown.
     */
    @Override
    public void close() {
        serviceDiscoveryAsyncClient.close();
    }

    private List<String> convertServiceIds(ListServicesResponse listServicesResult) {
        List<ServiceSummary> services = listServicesResult.services();
        List<String> serviceIds = new ArrayList<>(services.size());
        for (ServiceSummary service : services) {
            serviceIds.add(service.id());
        }
        return serviceIds;
    }

}
