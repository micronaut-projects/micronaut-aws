package io.micronaut.aws.xray.sampling

import com.amazonaws.xray.strategy.sampling.CentralizedSamplingStrategy
import com.amazonaws.xray.strategy.sampling.SamplingStrategy
import io.micronaut.aws.xray.ApplicationContextSpecification

class CentralizedSamplingStrategyFactorySpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'tracing.xray.sampling-rule': 'sampling-rules.json'
        ]
    }

    void 'you can provide a JSON Sampling rule definition with tracing.xray.sampling-rule'() {
        when:
        SamplingStrategy samplingStrategy = applicationContext.getBean(SamplingStrategy)

        then:
        samplingStrategy
        samplingStrategy instanceof CentralizedSamplingStrategy
    }
}
