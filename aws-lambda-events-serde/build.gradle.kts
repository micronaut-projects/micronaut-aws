plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    implementation(mnSerde.micronaut.serde.jackson)
    implementation(libs.managed.aws.lambda.events)
    testImplementation(libs.assertj.core)
    implementation(libs.managed.aws.lambda.java.serialization)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
