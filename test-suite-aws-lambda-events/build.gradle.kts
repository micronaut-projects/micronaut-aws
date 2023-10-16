plugins {
    id("java-library")
}
dependencies {
    api(libs.aws.lambda.java.runtimeinterfaceclient)
    api(libs.managed.aws.lambda.events)
    implementation(mnTest.micronaut.test.junit5)
    implementation(projects.micronautFunctionAws)
}
