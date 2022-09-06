plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(mn.micronaut.http.server)
    api(project(":function-aws-api-proxy"))
    implementation(libs.jetty.server)
    testImplementation(mn.micronaut.http.client)
}
