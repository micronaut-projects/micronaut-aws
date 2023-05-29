plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.micronautAwsSdkV2)
    api(libs.awssdk.cloudwatchlogs)
    api(mnSerde.micronaut.serde.jackson)

    implementation(mnLogging.logback.classic)
    implementation(libs.logback.json.classic) {
        exclude(group = "ch.qos.logback")
    }

    testRuntimeOnly(mn.snakeyaml)
}
