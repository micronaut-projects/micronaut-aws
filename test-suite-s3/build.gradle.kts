plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSerde.micronaut.serde.processor)

    implementation(projects.micronautAwsSdkV2)
    implementation(libs.awssdk.s3)
    implementation(mn.micronaut.http)
    implementation(mn.micronaut.http.server.netty)
    implementation(mnValidation.micronaut.validation)
    implementation(mnSerde.micronaut.serde.jackson)
    implementation(mnLogging.logback.classic)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnTest.junit.jupiter.params)
    testImplementation(projects.testSuiteUtils)
}
