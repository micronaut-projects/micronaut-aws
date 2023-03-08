plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.micronautAwsCommon)
    api(mnDiscoveryClient.micronaut.discovery.client)
    testImplementation(mn.micronaut.http.server.netty)
}
