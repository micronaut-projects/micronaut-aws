plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.discovery.core)
    api(projects.micronautAwsServiceDiscovery)
    api(projects.micronautAwsSdkV2)
    implementation(libs.aws.ssm)
    implementation(mn.reactor)
    testImplementation(mn.micronaut.http.server.netty)
}
