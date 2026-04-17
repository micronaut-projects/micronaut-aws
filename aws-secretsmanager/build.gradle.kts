plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.discovery.core)
    api(projects.micronautAwsSdkV2)
    api(projects.micronautAwsDistributedConfiguration)
    api(libs.awssdk.secretsmanager)
    
}
