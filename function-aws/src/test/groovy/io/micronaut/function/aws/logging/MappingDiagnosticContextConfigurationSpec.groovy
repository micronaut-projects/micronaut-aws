package io.micronaut.function.aws.logging

import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.function.aws.MicronautRequestHandler
import io.micronaut.function.aws.SquareHandler

class MappingDiagnosticContextConfigurationSpec extends ApplicationContextSpecification {

    void "bean of type MappingDiagnosticContextConfiguration exists and contains default values"() {
        when:
        MappingDiagnosticContextConfiguration config = applicationContext.getBean(MappingDiagnosticContextConfiguration)

        then:
        noExceptionThrown()
        "AWSRequestId" == config.awsRequestId
        "AWSFunctionName" == config.functionName
        "AWSFunctionVersion" == config.functionVersion
        "AWSFunctionArn" == config.functionArn
        "AWSFunctionMemoryLimit" == config.memoryLimit
        "AWSFunctionRemainingTime" == config.remainingTime
        "AWS-XRAY-TRACE-ID" == config.xrayTraceId
    }

    @Override
    MicronautRequestHandler instantiateHandler() {
        new SquareHandler([:])
    }
}
