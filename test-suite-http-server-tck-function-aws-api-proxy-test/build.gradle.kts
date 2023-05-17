plugins {
    id("io.micronaut.build.internal.aws.http-server-tck-module")
}

dependencies {
    testImplementation(projects.micronautFunctionAwsApiProxyTest)
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(mnValidation.micronaut.validation)
}
