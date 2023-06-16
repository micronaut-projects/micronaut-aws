plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
}
dependencies {
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautAwsLambdaEventsSerde)
}
