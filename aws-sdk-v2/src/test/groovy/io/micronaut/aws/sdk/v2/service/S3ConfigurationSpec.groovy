package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.service.s3.S3ConfigurationProperties
import io.micronaut.context.ApplicationContext
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1880")
class S3ConfigurationSpec extends Specification {

    void "path-style-access-enabled can be configured to be #enabled"() {
        when:
        ApplicationContext ctx = ApplicationContext.run(
                'aws.s3.path-style-access-enabled': enabled
        )
        S3ConfigurationProperties config = ctx.getBean(S3ConfigurationProperties)

        then:
        config.builder.pathStyleAccessEnabled() == enabled

        cleanup:
        ctx.close()

        where:
        enabled << [true, false]
    }
}
