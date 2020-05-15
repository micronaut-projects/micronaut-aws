package io.micronaut.aws.sdk.v2

import groovy.transform.NotYetImplemented
import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.apache.ApacheHttpClient
import spock.lang.Issue
import spock.lang.Specification

class ApacheClientSpec extends Specification {

    @NotYetImplemented
    @Issue("https://github.com/micronaut-projects/micronaut-core/issues/3245")
    void "apache client can be configured"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
                ['aws.apache-client.max-connections': 123]
        )

        when:
        ApacheHttpClient client = applicationContext.getBean(SdkHttpClient) as ApacheHttpClient

        then:
        client.resolvedOptions.get(SdkHttpConfigurationOption.MAX_CONNECTIONS) == 123
    }
}
