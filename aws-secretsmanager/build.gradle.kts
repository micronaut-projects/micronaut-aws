plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.micronautAwsSdkV2)
    api(projects.micronautAwsDistributedConfiguration)
    api(libs.awssdk.secretsmanager)
    implementation(mn.micronaut.jackson.databind)
}
