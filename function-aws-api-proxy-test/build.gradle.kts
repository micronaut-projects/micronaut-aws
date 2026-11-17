plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    api(mn.micronaut.http.server)
    api(projects.micronautFunctionAwsApiProxy)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
}

// ApiGatewayV2Test compares the API Gateway V2 handler against a real Netty server, and uses Micronaut Security and
// Micronaut Data (for Pageable). Those would change the behavior of the other tests in this module (e.g. Unauthorized
// responses, and an EmbeddedServer bean in the Lambda context), so they live in a separate source set.
val apiGatewayV2Test: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

configurations[apiGatewayV2Test.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[apiGatewayV2Test.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    add(apiGatewayV2Test.annotationProcessorConfigurationName, platform(libs.micronaut.security))
    add(apiGatewayV2Test.annotationProcessorConfigurationName, mn.micronaut.inject.java)
    add(apiGatewayV2Test.implementationConfigurationName, mnTest.junit.jupiter.api)
    add(apiGatewayV2Test.implementationConfigurationName, mn.micronaut.http.client)
    add(apiGatewayV2Test.implementationConfigurationName, mn.micronaut.jackson.databind)
    add(apiGatewayV2Test.implementationConfigurationName, platform(libs.micronaut.security))
    add(apiGatewayV2Test.implementationConfigurationName, "io.micronaut.security:micronaut-security")
    add(apiGatewayV2Test.implementationConfigurationName, "io.micronaut:micronaut-http-server-netty")
    add(apiGatewayV2Test.implementationConfigurationName, libs.micronaut.datajdbc)
    add(apiGatewayV2Test.runtimeOnlyConfigurationName, mnTest.junit.jupiter.engine)
    add(apiGatewayV2Test.runtimeOnlyConfigurationName, mnTest.junit.platform.launcher)
}

val apiGatewayV2TestTask = tasks.register<Test>("apiGatewayV2Test") {
    description = "Runs the API Gateway V2 tests, which need a real Netty server."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    useJUnitPlatform()
    testClassesDirs = apiGatewayV2Test.output.classesDirs
    classpath = apiGatewayV2Test.runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(apiGatewayV2TestTask)
}

tasks.named("checkstyle${apiGatewayV2Test.name.replaceFirstChar { it.uppercase() }}") {
    enabled = false // checkstyleTest is skipped for test code as well
}
