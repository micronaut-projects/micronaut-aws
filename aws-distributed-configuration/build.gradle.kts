plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.awsCommon)
    api(libs.micronaut.discovery.client)
    testImplementation(mn.micronaut.http.server.netty)
}
