package io.micronaut.aws.xray.configuration

import com.amazonaws.xray.AWSXRayRecorder
import io.micronaut.aws.xray.XRayRecorderFactory

import io.micronaut.aws.xray.sdkclients.SdkClientBuilderListener
import io.micronaut.context.ApplicationContext
import io.micronaut.aws.xray.cloudwatch.MetricsSegmentListenerFactory

import spock.lang.Specification

class XRayConfigurationSpec extends Specification {

    def "all integrations are enabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name": "test-application",
        ])

        expect:
        applicationContext.containsBean(AWSXRayRecorder)

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)

        then:
        xRayConfiguration.isEnabled()
        xRayConfiguration.isClientFilter()
        xRayConfiguration.isServerFilter()
        xRayConfiguration.isSdkClients()
        xRayConfiguration.isCloudWatchMetrics()

        cleanup:
        applicationContext.close()
    }

    def "it configures sampling rule"(){
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "tracing.xray.sampling-rule": "rule"
        ])

        when:
        XRayConfiguration configuration = applicationContext.getBean(XRayConfiguration)

        then:
        configuration.samplingRule.isPresent()
        configuration.samplingRule.get() == "rule"
    }

    def "it configures fixed segment for http filter"(){
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "tracing.xray.fixed-name": "fixed segment name",
        ])

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)

        then:
        xRayConfiguration.isServerFilter()
        xRayConfiguration.getFixedName().isPresent()
        xRayConfiguration.getFixedName().get() == "fixed segment name"
    }

    def "it disables clients"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "tracing.xray.enabled": false,
                "tracing.xray.sdk-clients": false,
                "tracing.xray.cloud-watch-metrics": false
        ])

        expect:
        !applicationContext.containsBean(XRayConfiguration)
        !applicationContext.containsBean(XRayRecorderFactory)
        !applicationContext.containsBean(SdkClientBuilderListener)
        !applicationContext.containsBean(MetricsSegmentListenerFactory)

        cleanup:
        applicationContext.close()
    }

    def "it globally disables features"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "tracing.xray.enabled": false,
                "tracing.xray.sdk-clients": true,
                "tracing.xray.cloud-watch-metrics": true
        ])

        expect:
        !applicationContext.containsBean(XRayConfiguration)
        !applicationContext.containsBean(XRayRecorderFactory)
        !applicationContext.containsBean(SdkClientBuilderListener)
        !applicationContext.containsBean(MetricsSegmentListenerFactory)

        cleanup:
        applicationContext.close()
    }
}
