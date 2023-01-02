plugins {
    id("io.micronaut.build.internal.http-test-module")
}
repositories {
    mavenCentral()
}
val micronautVersion: String by project
dependencies {
    testImplementation(projects.functionAwsApiProxy)
    testImplementation(mn.micronaut.http.server.tck)
    testImplementation(mn.micronaut.http.client)
    testImplementation(libs.junit.platform.engine)
}
