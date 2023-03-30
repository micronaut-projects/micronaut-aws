plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    api(platform(libs.boms.aws.java.sdk.v2))
    api(libs.awssdk.dynamodb)
    implementation(mnValidation.micronaut.validation)


    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)

    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(projects.micronautAwsSdkV2)

    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.http.client)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
