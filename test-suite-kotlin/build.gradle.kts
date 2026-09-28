plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("io.micronaut.build.internal.aws-tests")
}

val micronautVersion: String by project

dependencies {
    kaptTest(mn.micronaut.inject.java)
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testImplementation(mnTest.junit.jupiter.api)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionAwsTest)
    testImplementation(libs.kotlin.stdlib.jdk8)
    testImplementation(projects.micronautFunctionClientAwsV2)
    testImplementation(projects.micronautAwsSdkV1)
    testImplementation(libs.aws.java.sdk.s3)
    testImplementation(projects.micronautAwsSdkV2)
    testImplementation(projects.micronautAwsUa)
    testImplementation(libs.awssdk.s3)
    testImplementation(libs.awssdk.rekognition)
    testImplementation(projects.micronautAwsAlexa)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(mnValidation.micronaut.validation)
    testRuntimeOnly(libs.awssdk.url.connection.client)
    testRuntimeOnly(libs.jcl.over.slf4j)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mn.snakeyaml)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()

        systemProperty("aws.accessKeyId", "XXX")
        systemProperty("aws.secretKey", "YYY")
        systemProperty("aws.region", "us-east-1")
    }
}
