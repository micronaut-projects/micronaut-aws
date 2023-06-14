plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(mnSerde.micronaut.serde.api)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
