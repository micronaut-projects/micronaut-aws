plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(projects.micronautAwsSdkV2)
    testImplementation(libs.awssdk.sqs)
    testImplementation(libs.awssdk.url.connection.client)
    testImplementation(mn.micronaut.dev.tck)
    // the reload harness compiles the application under test with the processors on the test classpath
    testImplementation(mn.micronaut.inject.java)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.localstack)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnLogging.logback.classic)
}
