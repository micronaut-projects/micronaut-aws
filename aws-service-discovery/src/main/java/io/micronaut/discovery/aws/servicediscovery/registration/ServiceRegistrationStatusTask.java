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

import io.micronaut.core.annotation.Internal;
import io.micronaut.discovery.EmbeddedServerInstance;
import io.micronaut.discovery.ServiceInstance;
import io.micronaut.discovery.aws.servicediscovery.AwsServiceDiscoveryRegistrationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
import software.amazon.awssdk.services.servicediscovery.model.GetOperationRequest;
import software.amazon.awssdk.services.servicediscovery.model.GetOperationResponse;
import software.amazon.awssdk.services.servicediscovery.model.OperationStatus;

/**
 * This monitors and retries a given operationID when a service is registered. We have to do this in another thread because
 * amazon's async API still requires blocking polling to get the output of a service registration.
 *
 * @author Ryan
 * @author graemerocher
 */
@Internal
class ServiceRegistrationStatusTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrationStatusTask.class);

    private final String operationId;
    private final AwsServiceDiscoveryRegistrationConfiguration route53AutoRegistrationConfiguration;
    private final ServiceInstance embeddedServerInstance;
    private final ServiceDiscoveryClient serviceDiscoveryClient;
    private boolean registered = false;

    /**
     * Constructor for the task.
     *
     * @param serviceDiscoveryClient               aws discovery client
     * @param route53AutoRegistrationConfiguration configuration for auto registration
     * @param embeddedServerInstance               server instance running to register
     * @param operationId                          operation after first register call to monitor
     */
    ServiceRegistrationStatusTask(ServiceDiscoveryClient serviceDiscoveryClient,
                                  AwsServiceDiscoveryRegistrationConfiguration route53AutoRegistrationConfiguration,
                                  ServiceInstance embeddedServerInstance,
                                  String operationId) {
        this.serviceDiscoveryClient = serviceDiscoveryClient;
        this.route53AutoRegistrationConfiguration = route53AutoRegistrationConfiguration;
        this.embeddedServerInstance = embeddedServerInstance;
        this.operationId = operationId;

    }

    /**
     * Runs the polling process to AWS checks every 5 seconds.
     */
    @Override
    public void run() {
        while (!registered) {
            GetOperationResponse result = serviceDiscoveryClient.getOperation(
                    GetOperationRequest.builder().operationId(operationId).build()
            );
            LOG.info("Service registration for operation {} resulted in {}", operationId, result.operation().status());
            if (result.operation().status() == OperationStatus.FAIL || result.operation().status() == OperationStatus.SUCCESS) {
                registered = true; // either way we are done
                if (result.operation().status() == OperationStatus.FAIL) {
                    if (route53AutoRegistrationConfiguration.isFailFast() && embeddedServerInstance instanceof EmbeddedServerInstance) {
                        LOG.error("Error registering instance shutting down instance because failfast is set.");
                        ((EmbeddedServerInstance) embeddedServerInstance).getEmbeddedServer().stop();
                    }
                }
            }
        }
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            LOG.error("Registration monitor service has been aborted, unable to verify proper service registration on Route 53.", e);
        }
    }
}
