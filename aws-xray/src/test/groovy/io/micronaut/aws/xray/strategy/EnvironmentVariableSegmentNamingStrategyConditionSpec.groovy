package io.micronaut.aws.xray.strategy

import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import spock.lang.Specification

class EnvironmentVariableSegmentNamingStrategyConditionSpec extends Specification {
    void 'EnvironmentVariableSegmentNamingStrategyCondition evaluates to true if environment variable AWS_XRAY_TRACING_NAME is set'() {
        expect:
        new MockEnvironmentVariableSegmentNamingStrategyCondition().matches(null)
    }

    static class MockEnvironmentVariableSegmentNamingStrategyCondition extends EnvironmentVariableSegmentNamingStrategyCondition {
        Map<String, String> variables = ['AWS_XRAY_TRACING_NAME': 'bar']
        @Override
        @Nullable
        String getEnv(@NonNull String name) {
            variables[name]
        }
    }
}
