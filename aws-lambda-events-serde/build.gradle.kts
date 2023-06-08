plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(libs.managed.aws.lambda.events)
    api(mnSerde.micronaut.serde.jackson)
    implementation(libs.managed.aws.lambda.java.serialization)
    testImplementation(libs.assertj.core)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
