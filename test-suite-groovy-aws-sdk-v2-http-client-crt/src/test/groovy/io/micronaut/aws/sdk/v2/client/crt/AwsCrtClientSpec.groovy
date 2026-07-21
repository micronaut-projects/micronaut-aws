package io.micronaut.aws.sdk.v2.client.crt

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient
import software.amazon.awssdk.http.crt.AwsCrtHttpClient
import spock.lang.Specification

@MicronautTest(startApplication = false)
class AwsCrtClientSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "the CRT clients are elected when they are the only HTTP clients on the classpath"() {
        expect:
        beanContext.getBean(SdkHttpClient) instanceof AwsCrtHttpClient
        beanContext.getBean(SdkAsyncHttpClient) instanceof AwsCrtAsyncHttpClient
    }

    void "the CRT configuration and factory are BootstrapContextCompatible"() {
        expect:
        beanContext.getBeanDefinition(AwsCrtClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
        beanContext.getBeanDefinition(AwsCrtClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }
}
