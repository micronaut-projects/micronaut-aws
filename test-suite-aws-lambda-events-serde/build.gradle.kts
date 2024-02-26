plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws.http-server-tck-module")
}
dependencies {
    testImplementation(projects.testSuiteAwsLambdaEvents)
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(projects.micronautAwsLambdaEventsSerde)
}
