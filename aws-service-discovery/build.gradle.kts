plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(mn.micronaut.discovery)
    api(project(":aws-sdk-v2"))
    implementation(libs.awssdk.servicediscovery)
    testImplementation(libs.projectreactor)
    testImplementation(mn.micronaut.http.server.netty)
}
