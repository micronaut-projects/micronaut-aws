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
package io.micronaut.discovery.aws.route53.client;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.model.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.DiscoveryClient;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.aws.route53.AWSServiceDiscoveryResolver;
import io.micronaut.discovery.aws.route53.Route53ClientDiscoveryConfiguration;
import io.micronaut.discovery.aws.route53.Route53DiscoveryConfiguration;
import io.micronaut.discovery.aws.route53.registration.EC2ServiceInstance;
import org.reactivestreams.Publisher;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

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
@Singleton
@Requires(property = Route53AutoNamingClient.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Requires(classes = {AWSServiceDiscoveryAsync.class})
@Requires(env = Environment.AMAZON_EC2)
@Requires(beans = Route53DiscoveryConfiguration.class)
public class Route53AutoNamingClient implements DiscoveryClient {

    /**
     * Configuration property for whether route53 is enabled.
     */
    public static final String ENABLED = "aws.route53.discovery.enabled";

    private final AWSServiceDiscoveryResolver awsServiceDiscoveryResolver;
    private final Environment environment;

    private Route53ClientDiscoveryConfiguration route53ClientDiscoveryConfiguration;

    /**
     * Default constructor.
     *
     * @param route53ClientDiscoveryConfiguration The route 53 configuration
     * @param awsServiceDiscoveryResolver The AWS service discovery resolver
     * @param environment The environment
     */
    public Route53AutoNamingClient(Route53ClientDiscoveryConfiguration route53ClientDiscoveryConfiguration,
                                   AWSServiceDiscoveryResolver awsServiceDiscoveryResolver,
                                   Environment environment) {
        this.route53ClientDiscoveryConfiguration = route53ClientDiscoveryConfiguration;
        this.awsServiceDiscoveryResolver = awsServiceDiscoveryResolver;
        this.environment = environment;

    }

    /**
     * Used to help with testing.
     * @return Route53ClientDiscoveryConfiguration
     */
    public Route53ClientDiscoveryConfiguration getRoute53ClientDiscoveryConfiguration() {
        return route53ClientDiscoveryConfiguration;
    }

    /**
     * Used to help with testing.
     * @param route53ClientDiscoveryConfiguration config class
     */
    public void setRoute53ClientDiscoveryConfiguration(Route53ClientDiscoveryConfiguration route53ClientDiscoveryConfiguration) {
        this.route53ClientDiscoveryConfiguration = route53ClientDiscoveryConfiguration;
    }

    /**
     * The description.
     */
    @Override
    public String getDescription() {
        return "Route 53 Auto Naming Client";
    }

    /**
     * Gets a list of instances registered with Route53 given a service ID.
     * @param serviceId The service id
     * @return list of serviceInstances usable by MN.
     */
    @Override
    public Publisher<List<ServiceInstance>> getInstances(String serviceId) {
        if (serviceId == null) {
            serviceId = getRoute53ClientDiscoveryConfiguration().getAwsServiceId();  // we can default to the config file
        }
        ListInstancesRequest instancesRequest = new ListInstancesRequest().withServiceId(serviceId);
        return Mono.create(emitter -> {
            getDiscoveryClient().listInstancesAsync(instancesRequest, new AsyncHandler<ListInstancesRequest, ListInstancesResult>() {
                @Override
                public void onError(Exception exception) {
                    emitter.error(exception);
                }

                @Override
                public void onSuccess(ListInstancesRequest request, ListInstancesResult listInstancesResult) {
                    List<ServiceInstance> serviceInstances = new ArrayList<>();
                    for (InstanceSummary instanceSummary : listInstancesResult.getInstances()) {
                        try {
                            String uri = "http://" + instanceSummary.getAttributes().get("URI");
                            ServiceInstance serviceInstance = new EC2ServiceInstance(instanceSummary.getId(), new URI(uri)).metadata(instanceSummary.getAttributes()).build();
                            serviceInstances.add(serviceInstance);
                        } catch (URISyntaxException e) {
                            emitter.error(e);
                            return;
                        }
                    }
                    emitter.success(serviceInstances);
                }
            });
        });
    }

    /**
     * Gets a list of service IDs from AWS for a given namespace.
     * @return rx java publisher list of the service IDs in string format
     */
    @Override
    public Publisher<List<String>> getServiceIds() {
        ServiceFilter serviceFilter = new ServiceFilter().withName("NAMESPACE_ID").withValues(getRoute53ClientDiscoveryConfiguration().getNamespaceId());
        ListServicesRequest listServicesRequest = new ListServicesRequest().withFilters(serviceFilter);
        return Mono.create(emitter -> {
            getDiscoveryClient().listServicesAsync(listServicesRequest, new AsyncHandler<ListServicesRequest, ListServicesResult>() {
                @Override
                public void onError(Exception exception) {
                    emitter.error(exception);
                }

                @Override
                public void onSuccess(ListServicesRequest request, ListServicesResult listServicesResult) {
                    List<ServiceSummary> services = listServicesResult.getServices();
                    List<String> serviceIds = new ArrayList<>();

                    for (ServiceSummary service : services) {
                        serviceIds.add(service.getId());
                    }
                    emitter.success(serviceIds);
                }
            });
        });
    }

    /**
     * Close down AWS Client on shutdown.
     */
    @Override
    public void close() {
        getDiscoveryClient().shutdown();
    }

    /**
     * This is to make it easier to replace the client with a mock.
     * @return AWSServiceDiscoveryAsync to communicate with AWS
     */
    private AWSServiceDiscoveryAsync getDiscoveryClient() {
        return awsServiceDiscoveryResolver.resolve(environment);
    }
}
