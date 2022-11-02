plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.discovery)
    api(projects.awsSdkV2)
    implementation(libs.awssdk.servicediscovery)
    implementation(mn.micronaut.jackson.databind)
    testImplementation(mn.reactor)
    testImplementation(mn.micronaut.http.server.netty)
}
