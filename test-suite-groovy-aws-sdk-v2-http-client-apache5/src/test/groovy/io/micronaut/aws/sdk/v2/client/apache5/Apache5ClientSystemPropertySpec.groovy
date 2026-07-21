package io.micronaut.aws.sdk.v2.client.apache5

import io.micronaut.aws.sdk.v2.client.urlConnection.UrlConnectionClientFactory
import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.apache5.Apache5HttpClient
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class Apache5ClientSystemPropertySpec extends Specification {

    @RestoreSystemProperties
    void "the apache 5 client is elected when the http.service.impl system property selects it"() {
        given:
        System.setProperty(UrlConnectionClientFactory.HTTP_SERVICE_IMPL, 'software.amazon.awssdk.http.apache5.Apache5SdkHttpService')
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SdkHttpClient client = applicationContext.getBean(SdkHttpClient)

        then:
        client instanceof Apache5HttpClient

        cleanup:
        applicationContext.close()
    }
}
