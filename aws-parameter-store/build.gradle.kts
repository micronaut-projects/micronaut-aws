plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api("io.micronaut.discovery:micronaut-discovery-client")
    api(project(":aws-service-discovery"))
    api(project(":aws-sdk-v2"))
    implementation(libs.aws.ssm)
    implementation(libs.projectreactor)
    testImplementation(mn.micronaut.http.server.netty)
}
