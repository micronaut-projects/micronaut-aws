package io.micronaut.tracing.aws.configuration

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.tracing.aws.XRayRecorderFactory
import io.micronaut.tracing.aws.client.SdkClientBuilderListener
import io.micronaut.tracing.aws.cloudwatch.MetricsSegmentListenerFactory
import io.micronaut.tracing.aws.configuration.XRayCloudWatchMetricsConfiguration
import io.micronaut.tracing.aws.configuration.XRayConfiguration
import io.micronaut.tracing.aws.configuration.XRayHttpFilterConfiguration
import io.micronaut.tracing.aws.configuration.XRayHttpServerFilterConfiguration
import io.micronaut.tracing.aws.configuration.XRaySdkClientsConfiguration
import io.micronaut.tracing.aws.filter.XRayHttpServerFilter
import spock.lang.Specification

class XRayConfigurationSpec extends Specification {

    def "all integrations are enabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name": "test-application",
        ], Environment.AMAZON_EC2)

        when:
        XRayHttpFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayHttpFilterConfiguration)
        XRaySdkClientsConfiguration sdkClientsConfiguration = applicationContext.getBean(XRaySdkClientsConfiguration)
        XRayCloudWatchMetricsConfiguration cloudWatchMetricsConfiguration = applicationContext.getBean(XRayCloudWatchMetricsConfiguration)

        then:
        httpFilterConfiguration.isEnabled()
        sdkClientsConfiguration.isEnabled()
        cloudWatchMetricsConfiguration.isEnabled()
    }

    def "it configures sampling rule"(){
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "aws.xray.sampling-rule": "rule"
        ], Environment.AMAZON_EC2)

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
                "aws.xray.http-filter.server.fixedSegmentName": "fixed segment name",
        ], Environment.AMAZON_EC2)

        when:
        XRayHttpServerFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayHttpServerFilterConfiguration)

        then:
        httpFilterConfiguration.isEnabled()
        httpFilterConfiguration.getFixedSegmentName().isPresent()
        httpFilterConfiguration.getFixedSegmentName().get() == "fixed segment name"
    }

    def "it disables clients"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "aws.xray.enabled": false,
                "aws.xray.sdk-clients.enabled": false,
                "aws.xray.http-filter.enabled": false,
                "aws.xray.cloud-watch-metrics.enabled": false
        ], Environment.AMAZON_EC2)

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)
        XRayHttpFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayHttpFilterConfiguration)
        XRaySdkClientsConfiguration sdkClientsConfiguration = applicationContext.getBean(XRaySdkClientsConfiguration)
        XRayCloudWatchMetricsConfiguration cloudWatchMetricsConfiguration = applicationContext.getBean(XRayCloudWatchMetricsConfiguration)

        then:
        !xRayConfiguration.isEnabled()
        !httpFilterConfiguration.isEnabled()
        !sdkClientsConfiguration.isEnabled()
        !cloudWatchMetricsConfiguration.isEnabled()
    }

    def "it globally disables features"() {
        when:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "aws.xray.enabled": false,
                "aws.xray.sdk-clients.enabled": true,
                "aws.xray.http-filter.enabled": true,
                "aws.xray.cloudwatch.enabled": true
        ], Environment.AMAZON_EC2)

        then:
        !applicationContext.containsBean(XRayRecorderFactory)
        !applicationContext.containsBean(SdkClientBuilderListener)
        !applicationContext.containsBean(MetricsSegmentListenerFactory)
        !applicationContext.containsBean(XRayHttpServerFilter)
    }

}
