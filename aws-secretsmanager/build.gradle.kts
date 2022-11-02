plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.awsSdkV2)
    api(projects.awsDistributedConfiguration)
    api(libs.awssdk.secretsmanager)
    implementation(mn.micronaut.jackson.databind)
}
