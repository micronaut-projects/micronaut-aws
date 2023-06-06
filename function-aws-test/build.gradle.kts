plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    implementation(mnTest.micronaut.test.junit5)
    api(projects.micronautFunctionAws)
    api(mn.micronaut.function)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testRuntimeOnly(mn.snakeyaml)
}
