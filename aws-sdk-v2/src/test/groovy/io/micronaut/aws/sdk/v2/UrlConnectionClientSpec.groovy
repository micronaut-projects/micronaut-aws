package io.micronaut.aws.sdk.v2

import io.micronaut.aws.sdk.v2.client.UrlConnectionClientFactory
import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.SdkHttpConfigurationOption
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import java.time.Duration
import java.time.temporal.ChronoUnit

class UrlConnectionClientSpec extends Specification {

    @RestoreSystemProperties
    void "URL connection client can be configured"() {
        given:
        System.setProperty(UrlConnectionClientFactory.HTTP_SERVICE_IMPL, UrlConnectionClientFactory.URL_CONNECTION_SDK_HTTP_SERVICE)
        ApplicationContext applicationContext = ApplicationContext.run(
                ['aws.url-connection-client.connection-timeout': '13s']
        )

        when:
        UrlConnectionHttpClient client = applicationContext.getBean(SdkHttpClient) as UrlConnectionHttpClient

        then:
        client.options.get(SdkHttpConfigurationOption.CONNECTION_TIMEOUT) == Duration.of(13, ChronoUnit.SECONDS)
    }
}
