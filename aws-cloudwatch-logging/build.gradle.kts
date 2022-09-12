plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(project(":aws-sdk-v2"))
    implementation(libs.logback.json.classic)
    api(libs.awssdk.cloudwatchlogs)
    api(mn.micronaut.runtime)
    api(mn.micronaut.serde.jackson)

}

// TODO temporarily disable binary compatibility checks
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
