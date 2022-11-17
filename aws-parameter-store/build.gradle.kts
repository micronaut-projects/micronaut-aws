plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.discovery)
    api(projects.awsServiceDiscovery)
    api(projects.awsSdkV2)
    implementation(libs.aws.ssm)
    implementation(mn.reactor)
    testImplementation(mn.micronaut.http.server.netty)
}
