plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    api(mn.micronaut.http.server)
    api(projects.micronautFunctionAwsApiProxy)
    testAnnotationProcessor(platform(libs.micronaut.security))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(platform(libs.micronaut.security))
    testImplementation("io.micronaut.security:micronaut-security")
    testImplementation("io.micronaut:micronaut-http-server-netty")
    testImplementation(libs.micronaut.datajdbc)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
