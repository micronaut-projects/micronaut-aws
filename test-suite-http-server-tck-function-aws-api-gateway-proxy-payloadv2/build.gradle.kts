plugins {
    id("io.micronaut.build.internal.aws.http-server-tck-module")
}
dependencies {
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(projects.micronautFunctionAwsApiProxyTest)
    testImplementation(mnReactor.micronaut.reactor)
    testImplementation(mn.micronaut.jackson.databind)
}
