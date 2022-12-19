plugins {
    id("io.micronaut.build.internal.http-test-module")
}
repositories {
    mavenCentral()
}
val micronautVersion: String by project
dependencies {
    testImplementation(projects.functionAwsApiProxy)
    testImplementation(projects.httpServerTck)
    testImplementation(mn.micronaut.http.client)
}
