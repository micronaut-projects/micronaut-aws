package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.core.client.config.SdkClientOption

abstract class ServiceClientSpec<C,A> extends ApplicationContextSpecification {

    protected static final String ENDPOINT = "https://localhost:1234"

    abstract protected String serviceName()

    abstract protected C getClient();

    abstract protected A getAsyncClient();

    @Override
    Map<String, Object> getConfiguration() {
        String endpointOverrideProperty = "aws.services.${serviceName()}.endpoint-override"
        super.configuration + [
                (endpointOverrideProperty): ENDPOINT
        ]
    }

    void "it can configure a sync client"() {
        when:
        C client = getClient();

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        A client = getAsyncClient()

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        C client = getClient()
        A asyncClient = getAsyncClient()

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
