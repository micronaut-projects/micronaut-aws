plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(libs.micronaut.graal)
    compileOnly(projects.micronautFunctionAwsApiProxy)

    api(mn.micronaut.http.client)
    api(libs.managed.aws.lambda.events)
    implementation(projects.micronautAwsUa)
    testImplementation(projects.micronautFunctionAwsApiProxy) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-afterburner")
    }
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
}
