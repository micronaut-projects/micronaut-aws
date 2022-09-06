plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(project(":aws-common"))
    api(mn.micronaut.discovery)
    testImplementation(mn.micronaut.http.server.netty)
}
