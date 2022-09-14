package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification

abstract class ServiceClientSpec extends ApplicationContextSpecification {

    protected static final String ENDPOINT = "https://localhost:1234"

    abstract protected String serviceName()

    @Override
    Map<String, Object> getConfiguration() {
        String endpointOverrideProperty = "aws.services.${serviceName()}.endpoint-override"
        super.configuration + [
                (endpointOverrideProperty): ENDPOINT
        ]
    }
}
