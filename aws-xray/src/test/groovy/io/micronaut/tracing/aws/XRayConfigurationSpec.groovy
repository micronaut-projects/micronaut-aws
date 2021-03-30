package io.micronaut.tracing.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.tracing.aws.client.SdkClientBuilderListener
import io.micronaut.tracing.aws.cloudwatch.MetricsSegmentListenerFactory
import io.micronaut.tracing.aws.filter.XRayHttpServerFilter
import spock.lang.Specification

class XRayConfigurationSpec extends Specification {

    def "all integrations are enabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name": "test-application",
        ], Environment.AMAZON_EC2)

        when:
        XRayConfiguration.XRayHttpFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayConfiguration.XRayHttpFilterConfiguration)
        XRayConfiguration.SdkClientsConfiguration sdkClientsConfiguration = applicationContext.getBean(XRayConfiguration.SdkClientsConfiguration)
        XRayConfiguration.XRayCloudWatchMetricsConfiguration cloudWatchMetricsConfiguration = applicationContext.getBean(XRayConfiguration.XRayCloudWatchMetricsConfiguration)

        then:
        httpFilterConfiguration.isEnabled()
        sdkClientsConfiguration.isEnabled()
        cloudWatchMetricsConfiguration.isEnabled()
    }

    def "it configures sampling rule"(){
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                "micronaut.application.name" : "test-application",
                "aws.xray.samplingRule": "rule"
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
                "aws.xray.httpfilter.fixedSegmentName": "fixed segment name",
        ], Environment.AMAZON_EC2)

        when:
        XRayConfiguration.XRayHttpFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayConfiguration.XRayHttpFilterConfiguration)

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
                "aws.xray.sdkclients.enabled": false,
                "aws.xray.httpfilter.enabled": false,
                "aws.xray.cloudwatch.enabled": false
        ], Environment.AMAZON_EC2)

        when:
        XRayConfiguration xRayConfiguration = applicationContext.getBean(XRayConfiguration)
        XRayConfiguration.XRayHttpFilterConfiguration httpFilterConfiguration = applicationContext.getBean(XRayConfiguration.XRayHttpFilterConfiguration)
        XRayConfiguration.SdkClientsConfiguration sdkClientsConfiguration = applicationContext.getBean(XRayConfiguration.SdkClientsConfiguration)
        XRayConfiguration.XRayCloudWatchMetricsConfiguration cloudWatchMetricsConfiguration = applicationContext.getBean(XRayConfiguration.XRayCloudWatchMetricsConfiguration)

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
                "aws.xray.sdkclients.enabled": true,
                "aws.xray.httpfilter.enabled": true,
                "aws.xray.cloudwatch.enabled": true
        ], Environment.AMAZON_EC2)

        then:
        !applicationContext.containsBean(XRayRecorderFactory)
        !applicationContext.containsBean(SdkClientBuilderListener)
        !applicationContext.containsBean(MetricsSegmentListenerFactory)
        !applicationContext.containsBean(XRayHttpServerFilter)
    }

}
