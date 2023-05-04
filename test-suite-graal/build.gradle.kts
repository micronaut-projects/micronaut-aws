plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
    id("io.micronaut.build.internal.aws-native-tests")
}

dependencies {
    testImplementation(projects.micronautFunctionAwsApiProxy)
}
