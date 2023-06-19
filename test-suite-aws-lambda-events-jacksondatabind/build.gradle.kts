plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws.http-server-tck-module")
}
dependencies {
    testImplementation(projects.testSuiteAwsLambdaEvents)
    testImplementation(mn.micronaut.jackson.databind)
}
