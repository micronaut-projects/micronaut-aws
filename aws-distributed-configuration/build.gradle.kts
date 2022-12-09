plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.awsCommon)
    api(mn.micronaut.discovery.core)
    testImplementation(mn.micronaut.http.server.netty)
}
