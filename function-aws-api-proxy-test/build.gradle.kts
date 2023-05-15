plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.http.server)
    api(projects.micronautFunctionAwsApiProxy)
    implementation(libs.jetty.server)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
}
