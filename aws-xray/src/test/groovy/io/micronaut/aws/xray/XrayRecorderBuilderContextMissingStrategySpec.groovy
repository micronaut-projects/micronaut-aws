package io.micronaut.aws.xray

import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy

class XrayRecorderBuilderContextMissingStrategySpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'XrayRecorderBuilderContextMissingStrategySpec'
    }
    void "it is possible to customize AWSXRayRecorderBuilder via BeanCreatedEventListener"() {
        when:
        AWSXRayRecorderBuilder builder = applicationContext.getBean(AWSXRayRecorderBuilder)

        then:
        noExceptionThrown()
        builder.build().contextMissingStrategy instanceof LogErrorContextMissingStrategy
    }
}
