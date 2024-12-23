plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
    id("io.micronaut.build.internal.common")
}
dependencies {
    testImplementation(projects.micronautFunctionClientAws)
}

