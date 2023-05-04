plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)
    compileOnly(projects.micronautFunctionAwsApiProxy)

    api(mn.micronaut.http.client.jdk)
    api(libs.managed.aws.lambda.events)
    api(projects.micronautAwsUa)
    testImplementation(projects.micronautFunctionAwsApiProxy) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-afterburner")
    }
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
}
