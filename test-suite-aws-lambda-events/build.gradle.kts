plugins {
    id("java-library")
}
dependencies {
    implementation(libs.managed.aws.lambda.events)
    implementation(mnTest.micronaut.test.junit5)
    implementation(projects.micronautFunctionAws)
}
