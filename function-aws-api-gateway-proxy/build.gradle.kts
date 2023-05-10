plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    api(libs.managed.aws.lambda.events)
    api(mn.micronaut.http.server)
    api(mn.micronaut.http.client.core)
    api(projects.micronautFunctionAws)
    api(mnServlet.micronaut.servlet.core)
    implementation(mn.micronaut.http.netty)

    testImplementation(mn.micronaut.jackson.databind)
}

// TODO temporarily disable binary compatibility checks
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
