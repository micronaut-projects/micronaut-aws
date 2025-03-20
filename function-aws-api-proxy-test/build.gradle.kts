plugins {
    id("io.micronaut.build.internal.aws-module")
}
repositories {
    mavenLocal {
        mavenContent {
            snapshotsOnly()
        }
    }
}
dependencies {
    api(mn.micronaut.http.server)
    api(projects.micronautFunctionAwsApiProxy)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
}
