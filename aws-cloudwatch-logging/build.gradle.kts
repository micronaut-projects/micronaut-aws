plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(project(":aws-sdk-v2"))
    implementation(libs.logback.json.classic)
    implementation(libs.jackson.databind)
    api(libs.awssdk.cloudwatchlogs)
    api(mn.micronaut.runtime)

}

// TODO temporarily disable binary compatibility checks
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
