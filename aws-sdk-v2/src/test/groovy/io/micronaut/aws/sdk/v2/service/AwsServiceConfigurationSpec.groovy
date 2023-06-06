package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification

class AwsServiceConfigurationSpec extends Specification {

    void "by default no bean of type AwsServiceConfiguration with s3 qualifier name exists"() {
        given:
        ApplicationContext context = ApplicationContext.run()
        expect:
        !context.containsBean(AWSServiceConfiguration, Qualifiers.byName(S3Client.SERVICE_NAME))

        cleanup:
        context.close()
    }
}
