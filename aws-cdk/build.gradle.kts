plugins {
    id("io.micronaut.build.internal.aws-module")
}

val micronautVersion: String by project
val micronautStarterVersion: String by project

dependencies {
    api(libs.aws.cdk.lib)
    api("io.micronaut.starter:micronaut-starter-api:$micronautStarterVersion")
    testImplementation(projects.functionAwsApiProxy)
}
