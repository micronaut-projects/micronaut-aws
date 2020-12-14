package io.micronaut.aws.sdk.v2.client

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.apache.ApacheHttpClient

class ApacheClientSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.apache-client.max-connections': 123,
                'aws.apache-client.proxy.username': 'username'
        ]
    }

    void "apache client can be configured"() {
        when:
        ApacheHttpClient client = applicationContext.getBean(SdkHttpClient) as ApacheHttpClient

        then:
        client.resolvedOptions.get(SdkHttpConfigurationOption.MAX_CONNECTIONS) == 123
        client.requestConfig.proxyConfiguration().username() == 'username'
    }
}
