plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
}
dependencies {
    testImplementation(projects.micronautAwsSdkV2)
}
