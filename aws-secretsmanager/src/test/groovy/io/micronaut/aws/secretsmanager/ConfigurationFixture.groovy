package io.micronaut.aws.secretsmanager

interface ConfigurationFixture {

    default Map<String, Object> getConfiguration() {
        Map<String, Object> m = [:]
        if (specName) {
            m['spec.name'] = specName
        }
        m
    }

    default String getSpecName() {
        null
    }
}