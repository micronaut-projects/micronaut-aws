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
package io.micronaut.discovery.aws.servicediscovery.registration;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.EmbeddedServerInstance;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryRegistrationConfiguration;
import io.micronaut.discovery.client.registration.DiscoveryServiceAutoRegistration;
import io.micronaut.discovery.cloud.ComputeInstanceMetadata;
import io.micronaut.discovery.cloud.aws.AmazonComputeInstanceMetadataResolver;
import io.micronaut.health.HealthStatus;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.scheduling.TaskExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
import software.amazon.awssdk.services.servicediscovery.model.CustomHealthStatus;
import software.amazon.awssdk.services.servicediscovery.model.DeregisterInstanceRequest;
import software.amazon.awssdk.services.servicediscovery.model.RegisterInstanceRequest;
import software.amazon.awssdk.services.servicediscovery.model.RegisterInstanceResponse;
import software.amazon.awssdk.services.servicediscovery.model.Service;
import software.amazon.awssdk.services.servicediscovery.model.UpdateInstanceCustomHealthStatusRequest;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


/**
 * An implementation of {@link DiscoveryServiceAutoRegistration} for Route 53.
 *
 * @author Rvanderwerf
 * @author graemerocher
 * @author Denis Stepanov
 * @since 1.0
 */
@Internal
@Singleton
@Requires(classes = ServiceDiscoveryClient.class)
@Requires(env = Environment.AMAZON_EC2)
@Requires(beans = {AwsServiceDiscoveryRegistrationConfiguration.class})
@Requires(property = AwsServiceDiscoveryRegistrationClient.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Requires(property = ApplicationConfiguration.APPLICATION_NAME)
public class AwsServiceDiscoveryRegistrationClient extends DiscoveryServiceAutoRegistration {

    /**
     * Constant for AWS instance port.
     */
    public static final String AWS_INSTANCE_PORT = "AWS_INSTANCE_PORT";

    /**
     * Constant for AWS intance IPv4.
     */
    public static final String AWS_INSTANCE_IPV4 = "AWS_INSTANCE_IPV4";

    /**
     * Constant for AWS instance cname.
     */
    public static final String AWS_INSTANCE_CNAME = "AWS_INSTANCE_CNAME";

    /**
     * Constant for AWS instance IPv6.
     */
    public static final String AWS_INSTANCE_IPV6 = "AWS_INSTANCE_IPV6";

    /**
     * Constant for AWS alias dns name.
     */
    public static final String AWS_ALIAS_DNS_NAME = "AWS_ALIAS_DNS_NAME";
    /**
     * Constant for whether route 53 registration is enabled.
     */
    public static final String ENABLED = "aws.service-discovery.registration.enabled";

    private static final Logger LOG = LoggerFactory.getLogger(AwsServiceDiscoveryRegistrationClient.class);
    private final AwsServiceDiscoveryRegistrationConfiguration route53AutoRegistrationConfiguration;
    private final Environment environment;
    private final AmazonComputeInstanceMetadataResolver amazonComputeInstanceMetadataResolver;
    private Service discoveryService;
    private final Executor executorService;
    private final ServiceDiscoveryClient serviceDiscoveryClient;
    private final ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient;

    /**
     * Constructor for setup.
     *
     * @param environment                                  current environment
     * @param awsServiceDiscoveryRegistrationConfiguration config for auto registration
     * @param amazonComputeInstanceMetadataResolver        resolver for aws compute metdata
     * @param executorService                              this is for executing the thread to monitor the register operation for completion
     * @param serviceDiscoveryClient                       the serviceDiscoveryClient
     * @param serviceDiscoveryAsyncClient                  the serviceDiscoveryAsyncClient
     */
    protected AwsServiceDiscoveryRegistrationClient(
            Environment environment,
            AwsServiceDiscoveryRegistrationConfiguration awsServiceDiscoveryRegistrationConfiguration,
            AmazonComputeInstanceMetadataResolver amazonComputeInstanceMetadataResolver,
            @Named(TaskExecutors.IO) Executor executorService,
            ServiceDiscoveryClient serviceDiscoveryClient,
            ServiceDiscoveryAsyncClient serviceDiscoveryAsyncClient) {
        super(awsServiceDiscoveryRegistrationConfiguration);
        this.environment = environment;
        this.route53AutoRegistrationConfiguration = awsServiceDiscoveryRegistrationConfiguration;
        this.serviceDiscoveryClient = serviceDiscoveryClient;
        this.serviceDiscoveryAsyncClient = serviceDiscoveryAsyncClient;
        this.amazonComputeInstanceMetadataResolver = amazonComputeInstanceMetadataResolver;
        this.executorService = executorService;
    }

    /**
     * If custom health check is enabled, this sends a heartbeat to it.
     * In most cases aws monitoring works off polling an application's endpoint
     *
     * @param instance The instance of the service
     * @param status   The {@link HealthStatus}
     */
    @Override
    protected void pulsate(ServiceInstance instance, HealthStatus status) {
        // this only work if you create a health status check when you register it
        // we can't really pulsate anywhere because amazon health checks work inverse from this UNLESS you have a custom health check
        Optional<String> opt = instance.getInstanceId();
        if (!opt.isPresent()) {
            // try the metadata
            if (instance.getMetadata().contains("instanceId")) {
                opt = Optional.of(instance.getMetadata().asMap().get("instanceId"));
            } else {
                LOG.error("Cannot determine the instance ID. Are you sure you are running on AWS EC2?");
            }
        }

        opt.ifPresent(instanceId -> {
            if (discoveryService != null && discoveryService.healthCheckConfig() != null) {
                CustomHealthStatus customHealthStatus = CustomHealthStatus.UNHEALTHY;

                if (status.getOperational().isPresent()) {
                    customHealthStatus = CustomHealthStatus.HEALTHY;
                }

                serviceDiscoveryClient.updateInstanceCustomHealthStatus(
                        UpdateInstanceCustomHealthStatusRequest
                                .builder()
                                .instanceId(instanceId)
                                .serviceId(route53AutoRegistrationConfiguration.getAwsServiceId())
                                .status(customHealthStatus)
                                .build()
                );
            }

            if (status.getOperational().isPresent() && !status.getOperational().get()) {
                serviceDiscoveryClient.deregisterInstance(
                        DeregisterInstanceRequest.builder().instanceId(instanceId).serviceId(route53AutoRegistrationConfiguration.getAwsServiceId()).build()
                );
                LOG.info("Health status is non operational, instance id {} was de-registered from the discovery service.", instanceId);
            }

        });

    }

    /**
     * shutdown instance if it fails health check can gracefully stop.
     */
    @Override
    public void deregister(ServiceInstance instance) {
        if (instance.getInstanceId().isPresent()) {
            serviceDiscoveryClient.deregisterInstance(
                    DeregisterInstanceRequest.builder()
                            .serviceId(route53AutoRegistrationConfiguration.getAwsServiceId())
                            .instanceId(instance.getInstanceId().get())
                            .build()
            );
        }
    }

    /**
     * register new instance to the service registry.
     *
     * @param instance The {@link ServiceInstance}
     */
    @Override
    public void register(ServiceInstance instance) {
        // step 1 get domain from config
        // set service from config
        // check if service exists
        // register service if not
        // register instance to service

        Map<String, String> instanceAttributes = new HashMap<>();

        // you can't just put anything in there like a custom config. Only certain things are allowed or you get weird errors
        // see https://docs.aws.amazon.com/Route53/latest/APIReference/API_autonaming_RegisterInstance.html
        //if the service uses A records use these
        if (instance.getPort() > 0) {
            instanceAttributes.put("AWS_INSTANCE_PORT", Integer.toString(instance.getPort()));
        }
        if (amazonComputeInstanceMetadataResolver != null) {
            Optional<ComputeInstanceMetadata> instanceMetadata = amazonComputeInstanceMetadataResolver.resolve(environment);
            if (instanceMetadata.isPresent()) {
                ComputeInstanceMetadata computeInstanceMetadata = instanceMetadata.get();
                if (computeInstanceMetadata.getPublicIpV4() != null) {
                    instanceAttributes.put(AWS_INSTANCE_IPV4, computeInstanceMetadata.getPublicIpV4());
                } else {
                    if (computeInstanceMetadata.getPrivateIpV4() != null) {
                        instanceAttributes.put(AWS_INSTANCE_IPV4, computeInstanceMetadata.getPrivateIpV4());
                    }
                }

                if (!instanceAttributes.containsKey(AWS_INSTANCE_IPV4)) {
                    // try ip v6
                    if (computeInstanceMetadata.getPublicIpV4() != null) {
                        instanceAttributes.put(AWS_INSTANCE_IPV6, computeInstanceMetadata.getPublicIpV6());
                    } else {
                        if (computeInstanceMetadata.getPrivateIpV6() != null) {
                            instanceAttributes.put(AWS_INSTANCE_IPV6, computeInstanceMetadata.getPrivateIpV6());
                        }
                    }
                }
            }
        }

        ConvertibleValues<String> metadata = instance.getMetadata();

        String instanceId = null;
        if (instance.getInstanceId().isPresent()) {
            instanceId = instance.getInstanceId().get();
        } else {
            // try the metadata
            if (metadata.contains("instanceId")) {
                instanceId = metadata.asMap().get("instanceId");
            } else {
                LOG.error("Cannot determine the instance ID. Are you sure you are running on AWS EC2?");
            }
        }

        CompletableFuture<RegisterInstanceResponse> instanceResult = serviceDiscoveryAsyncClient.registerInstance(
                RegisterInstanceRequest.builder()
                        .serviceId(route53AutoRegistrationConfiguration.getAwsServiceId())
                        .instanceId(instanceId)
                        .creatorRequestId(Long.toString(System.nanoTime()))
                        .attributes(instanceAttributes)
                        .build()
        );

        LOG.info("Called AWS to register service [{}] with {}", instance.getId(), route53AutoRegistrationConfiguration.getAwsServiceId());

        instanceResult.whenComplete((registerInstanceResponse, throwable) -> {
            if (throwable != null) {
                LOG.error("Error registering instance with AWS: {}", throwable.getMessage(), throwable);
                if (route53AutoRegistrationConfiguration.isFailFast() && instance instanceof EmbeddedServerInstance) {
                    LOG.error("Error registering instance with AWS and Failfast is set: stopping instance");
                    ((EmbeddedServerInstance) instance).getEmbeddedServer().stop();
                }
            } else {
                if (registerInstanceResponse.operationId() != null) {
                    ServiceRegistrationStatusTask serviceRegistrationStatusTask = new ServiceRegistrationStatusTask(serviceDiscoveryClient,
                            route53AutoRegistrationConfiguration,
                            instance,
                            registerInstanceResponse.operationId());
                    executorService.execute(serviceRegistrationStatusTask);
                    LOG.info("Success calling register service request [{}] with {} is complete.", instance.getId(),
                            route53AutoRegistrationConfiguration.getAwsServiceId());
                }
            }
        });
    }

}
