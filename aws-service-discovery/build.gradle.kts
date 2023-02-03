plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mnDiscoveryClient.micronaut.discovery.client)
    api(projects.awsSdkV2)
    implementation(libs.awssdk.servicediscovery)
    implementation(mn.micronaut.jackson.databind)
    testImplementation(mn.reactor)
    testImplementation(mn.micronaut.http.server.netty)
}
