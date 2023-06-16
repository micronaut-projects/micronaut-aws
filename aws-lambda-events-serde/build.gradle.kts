plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(mnSerde.micronaut.serde.api)
    api(libs.managed.aws.lambda.events)
    implementation(libs.managed.aws.lambda.java.serialization)
    testImplementation(libs.assertj.core)
    testImplementation(mnSerde.micronaut.serde.jackson)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
