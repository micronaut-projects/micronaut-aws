plugins {
    id("groovy")
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.spock)
    testImplementation(mnTest.junit.jupiter.api)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionAwsTest)
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

java {
    sourceCompatibility = JavaVersion.toVersion("25")
    targetCompatibility = JavaVersion.toVersion("25")
}
