plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    compileOnly(mn.micronaut.discovery.core)
    implementation(mn.micronaut.jackson.databind)
    testImplementation(mn.micronaut.discovery.core)
}
