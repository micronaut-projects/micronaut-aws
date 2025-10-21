plugins {
    id("io.micronaut.application")
}

dependencies {
    implementation(projects.micronautAwsCloudwatchLogging)
    testImplementation(mnTest.micronaut.test.junit5)
}

micronaut {
    importMicronautPlatform = false
    runtime("netty")
    testRuntime("junit5")
}
