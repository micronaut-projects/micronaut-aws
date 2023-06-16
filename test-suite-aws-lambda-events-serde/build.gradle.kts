plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
}
dependencies {
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautAwsLambdaEventsSerde)
}
