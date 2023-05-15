plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    compileOnly(projects.micronautFunctionAwsApiProxy)
    api(libs.managed.aws.lambda.events)
    api(projects.micronautAwsUa)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(projects.micronautFunctionAws)

    //Adding both causes
    // Error instantiating bean of type  [io.micronaut.http.server.RouteExecutor]
    // Message: Binder registry is not mutable
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(mn.micronaut.http.server.netty)
}
