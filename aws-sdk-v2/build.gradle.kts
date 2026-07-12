plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(platform(libs.boms.aws.java.sdk.v2))
    api(projects.micronautAwsCommon)
    compileOnly(libs.graal.sdk)
    implementation(projects.micronautAwsUa)

    // Clients
    compileOnly(libs.awssdk.url.connection.client)
    compileOnly(libs.awssdk.netty.nio.client)
    compileOnly(libs.awssdk.apache.client)
    compileOnly(libs.awssdk.apache5.client)

    // Services
    compileOnly(libs.awssdk.apigatewaymanagementapi)
    compileOnly(libs.awssdk.s3)
    compileOnly(libs.awssdk.dynamodb)
    compileOnly(libs.awssdk.ses)
    compileOnly(libs.awssdk.sns)
    compileOnly(libs.awssdk.sqs)
    compileOnly(libs.awssdk.ssm)
    compileOnly(libs.awssdk.secretsmanager)
    compileOnly(libs.awssdk.servicediscovery)
    compileOnly(libs.awssdk.cloudwatchlogs)
    compileOnly(libs.awssdk.lambda)

    // Tests
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.awssdk.cloudwatchlogs)
    testImplementation(libs.awssdk.apigatewaymanagementapi)
    testImplementation(libs.awssdk.servicediscovery)
    testImplementation(libs.awssdk.url.connection.client)
    testImplementation(libs.awssdk.netty.nio.client)
    testImplementation(libs.awssdk.apache5.client)
    testImplementation(libs.awssdk.s3)
    testImplementation(libs.awssdk.dynamodb)
    testImplementation(libs.awssdk.ses)
    testImplementation(libs.awssdk.secretsmanager)
    testImplementation(libs.awssdk.sns)
    testImplementation(libs.awssdk.sqs)
    testImplementation(libs.awssdk.ssm)
    testImplementation(libs.awssdk.rekognition)
    testImplementation(libs.awssdk.lambda)
    testRuntimeOnly(libs.jcl.over.slf4j)

    testRuntimeOnly(mn.snakeyaml)
    testRuntimeOnly(mn.micronaut.jackson.databind)
}

// The AWS SDK rejects having more than one sync HTTP client implementation on the classpath, so the
// legacy Apache HttpClient 4.x client cannot share a classpath with the apache5-client used by the
// main test suite. This second source set exercises the 4.x client in isolation.
val testApache4 = sourceSets.create("testApache4")

configurations {
    named(testApache4.implementationConfigurationName) {
        extendsFrom(configurations.testImplementation.get())
        exclude(group = "software.amazon.awssdk", module = "apache5-client")
    }
    named(testApache4.runtimeOnlyConfigurationName) {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
}

dependencies {
    add(testApache4.implementationConfigurationName, sourceSets["main"].output)
    // The main test output is on the classpath only to reuse shared base classes such as
    // ApplicationContextSpecification. It also contains apache5-referencing specs compiled against
    // apache5-client (which is excluded here), but those are never loaded: the testApache4 task runs
    // only testApache4's own testClassesDirs, so the excluded client is never resolved at runtime.
    add(testApache4.implementationConfigurationName, sourceSets["test"].output)
    add(testApache4.implementationConfigurationName, libs.awssdk.apache.client)
    add(testApache4.annotationProcessorConfigurationName, mn.micronaut.inject.java)
}

val testApache4Task = tasks.register<Test>("testApache4") {
    description = "Runs tests for the legacy Apache HttpClient 4.x client."
    group = "verification"
    testClassesDirs = testApache4.output.classesDirs
    classpath = testApache4.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(testApache4Task)
}
