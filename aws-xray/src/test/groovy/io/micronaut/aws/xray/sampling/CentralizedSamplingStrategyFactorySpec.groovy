package io.micronaut.aws.xray.sampling

import com.amazonaws.xray.strategy.sampling.CentralizedSamplingStrategy
import com.amazonaws.xray.strategy.sampling.SamplingRequest
import com.amazonaws.xray.strategy.sampling.SamplingResponse
import com.amazonaws.xray.strategy.sampling.SamplingStrategy
import io.micronaut.context.ApplicationContext
import spock.lang.Specification
import spock.lang.Unroll

class CentralizedSamplingStrategyFactorySpec extends Specification {

    @Unroll
    void 'you can provide a JSON Sampling rule definition with tracing.xray.sampling-rule'(Map<String, Object> configuration) {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(configuration)

        String rule = configuration['tracing.xray.sampling-rule']
        if (rule.startsWith('file:')) {
            rule = rule.substring(5)
            assert new File(rule).exists()
        }

        expect:
        applicationContext.containsBean(SamplingStrategy)

        when:
        SamplingStrategy samplingStrategy = applicationContext.getBean(SamplingStrategy)

        then:
        samplingStrategy
        samplingStrategy instanceof CentralizedSamplingStrategy

        where:
        configuration << [
                ['tracing.xray.sampling-rule': 'classpath:sampling-rules.json'],
                ['tracing.xray.sampling-rule': "file:${new File('src/test/resources/sampling-rules.json').absolutePath}"],
        ]
    }
}
