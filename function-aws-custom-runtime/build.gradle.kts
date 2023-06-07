plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)
    compileOnly(projects.micronautFunctionAwsApiProxy)
    api(libs.managed.aws.lambda.events)
    api(projects.micronautAwsUa)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(projects.micronautFunctionAws)

    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(mn.micronaut.http.server.netty)
}
