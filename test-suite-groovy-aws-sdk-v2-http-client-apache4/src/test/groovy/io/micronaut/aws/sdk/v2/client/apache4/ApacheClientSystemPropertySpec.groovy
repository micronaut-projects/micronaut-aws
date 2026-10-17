package io.micronaut.aws.sdk.v2.client.apache4

import io.micronaut.aws.sdk.v2.client.urlConnection.UrlConnectionClientFactory
import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache.ApacheHttpClient
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class ApacheClientSystemPropertySpec extends Specification {

    @RestoreSystemProperties
    void "the apache client is created once when selected by the system property"() {
        given:
        System.setProperty(UrlConnectionClientFactory.HTTP_SERVICE_IMPL,
            'software.amazon.awssdk.http.apache.ApacheSdkHttpService')
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SdkHttpClient client = applicationContext.getBean(SdkHttpClient)

        then:
        client instanceof ApacheHttpClient
        applicationContext.getBeansOfType(SdkHttpClient).size() == 1

        cleanup:
        applicationContext.close()
    }
}
