plugins {
    id("io.micronaut.build.internal.aws-module")
}

val micronautVersion: String by project
val micronautStarterVersion: String by project

dependencies {
    api(libs.aws.cdk.lib)
    api(libs.micronaut.starter)
    testImplementation(projects.functionAwsApiProxy)
}
