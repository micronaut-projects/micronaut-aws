package io.micronaut.docs.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.services.s3.S3ClientBuilder
import spock.lang.Specification

@Property(name = "spec.name", value = "S3ClientBuilderSpec")
@MicronautTest(startApplication = false)
class S3ClientBuilderSpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    void "builders can be customised"() {
        when:
        S3ClientBuilder builder = applicationContext.getBean(S3ClientBuilder)

        then:
        noExceptionThrown()
        builder.overrideConfiguration().retryPolicy().present
        builder.overrideConfiguration().retryPolicy().get().retryMode() == RetryMode.LEGACY
    }
}
