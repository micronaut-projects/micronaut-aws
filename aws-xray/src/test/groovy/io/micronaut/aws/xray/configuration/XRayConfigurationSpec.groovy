package io.micronaut.aws.xray.configuration

import com.amazonaws.xray.AWSXRayRecorder
import io.micronaut.aws.xray.decorators.UserSegmentDecorator
import io.micronaut.aws.xray.recorder.XRayRecorderFactory
import io.micronaut.aws.xray.filters.client.XRayHttpClientFilter
import io.micronaut.aws.xray.decorators.SegmentDecorator
import io.micronaut.aws.xray.sdkclients.SdkClientBuilderListener
import io.micronaut.aws.xray.filters.server.XRayHttpServerFilter
import io.micronaut.aws.xray.strategy.SegmentNamingStrategy
import io.micronaut.context.ApplicationContext
import io.micronaut.aws.xray.cloudwatch.MetricsSegmentListenerFactory
import io.micronaut.core.util.StringUtils
import spock.lang.Specification

class XRayConfigurationSpec extends Specification {

    def "all integrations are enabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        expect:
        applicationContext.containsBean(AWSXRayRecorder)

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)

        then:
        !xRayConfiguration.getExcludes().isPresent()
        !xRayConfiguration.getSamplingRule().isPresent()
        xRayConfiguration.isServerFilter()
        xRayConfiguration.isUserSegmentDecorator()
        applicationContext.containsBean(UserSegmentDecorator)
        xRayConfiguration.isClientFilter()
        xRayConfiguration.isCloudWatchMetrics()
        xRayConfiguration.isSdkClients()
        !xRayConfiguration.getFixedName().isPresent()

        cleanup:
        applicationContext.close()
    }

    def "it configures sampling rule"(){
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "tracing.xray.sampling-rule": "rule",
                "tracing.xray.excludes": ["/health", "/assets/**"],
                "tracing.xray.server-filter": StringUtils.FALSE,
                "tracing.xray.client-filter": StringUtils.FALSE,
                "tracing.xray.cloud-watch-metrics": StringUtils.FALSE,
                "tracing.xray.sdk-clients": StringUtils.FALSE,
                "tracing.xray.fixed-name": 'micronautapp',
        ])

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)

        then:
        xRayConfiguration.getExcludes().isPresent()
        xRayConfiguration.getExcludes().get() == ["/health", "/assets/**"]
        xRayConfiguration.getSamplingRule().isPresent()
        !xRayConfiguration.isServerFilter()
        !xRayConfiguration.isClientFilter()
        !xRayConfiguration.isCloudWatchMetrics()
        !xRayConfiguration.isSdkClients()
        xRayConfiguration.getFixedName().get() == 'micronautapp'

        cleanup:
        applicationContext.close()
    }

    def "if you set tracing.xray.enabled: false no beans are loaded"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "tracing.xray.enabled": false,
                "tracing.xray.sdk-clients": true,
                "tracing.xray.cloud-watch-metrics": true
        ])

        expect:
        !applicationContext.containsBean(XRayConfiguration)
        !applicationContext.containsBean(XRayRecorderFactory)
        !applicationContext.containsBean(XRayHttpServerFilter)
        !applicationContext.containsBean(XRayHttpClientFilter)
        !applicationContext.containsBean(SegmentNamingStrategy)
        !applicationContext.containsBean(SdkClientBuilderListener)
        !applicationContext.containsBean(SegmentDecorator)
        !applicationContext.containsBean(MetricsSegmentListenerFactory)

        cleanup:
        applicationContext.close()
    }
}
