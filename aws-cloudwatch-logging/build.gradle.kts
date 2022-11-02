plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.awsSdkV2)
    implementation(libs.logback.json.classic)
    api(libs.awssdk.cloudwatchlogs)
    api(mn.micronaut.runtime)
    api(mn.micronaut.serde.jackson)
    testRuntimeOnly(mn.snakeyaml)
}
