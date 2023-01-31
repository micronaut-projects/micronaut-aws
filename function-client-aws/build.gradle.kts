plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.awsSdkV1)
    implementation(libs.aws.java.sdk.lambda)
    implementation(mn.reactor)
    api(mn.micronaut.function.client)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.function.web)
    testImplementation(mn.micronaut.function.groovy)
    testImplementation(mn.micronaut.runtime.groovy)
}
