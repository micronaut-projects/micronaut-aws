plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.micronautAwsSdkV2)
    implementation(libs.awssdk.lambda)
    implementation(mn.reactor)
    api(mn.micronaut.function.client)
    implementation(mn.micronaut.jackson.databind)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.function.web)
    testImplementation(mnGroovy.micronaut.function.groovy)
    testImplementation(mnGroovy.micronaut.runtime.groovy)
}
