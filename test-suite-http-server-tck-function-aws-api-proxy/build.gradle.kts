plugins {
    id("io.micronaut.build.internal.aws.http-server-tck-module")
}
dependencies {
    testImplementation(projects.micronautFunctionAwsApiProxy)
}
