plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(projects.micronautAwsSdkV2)
    api(libs.awssdk.cloudwatchlogs)
    api(mnSerde.micronaut.serde.jackson)

    implementation(mnLogging.logback.classic)
    implementation(libs.logback.json.classic) {
        // Exclude group and module for the POM
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }

    testRuntimeOnly(mn.snakeyaml)
}
