package io.micronaut.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class EnvironmentAwsRegionProviderSpec extends Specification {

    static final String TEST_REGION = "eu-west-43"

    void "region can be read from the environment"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                (EnvironmentAwsRegionProvider.REGION_ENV_VAR): TEST_REGION
        ])

        when:
        Region region = applicationContext.getBean(AwsRegionProvider).region

        then:
        region.toString() == TEST_REGION

        cleanup:
        applicationContext.close()
    }

    void "region can be read via yaml configuration"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run("yaml")

        when:
        Region region = applicationContext.getBean(AwsRegionProvider).region

        then:
        region.toString() == TEST_REGION

        cleanup:
        applicationContext.close()
    }

    @RestoreSystemProperties
    void "region can still be read from default places like system properties"() {
        given:
        System.setProperty(EnvironmentAwsRegionProvider.REGION_ENV_VAR, TEST_REGION)
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        Region region = applicationContext.getBean(AwsRegionProvider).region

        then:
        region.toString() == TEST_REGION

        cleanup:
        applicationContext.close()
    }

}
