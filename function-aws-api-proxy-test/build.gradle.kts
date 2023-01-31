plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.http.server)
    api(projects.functionAwsApiProxy)
    implementation(libs.jetty.server)
    testImplementation(mn.micronaut.http.client)
}
