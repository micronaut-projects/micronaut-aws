plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
}
dependencies {
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionClientAws)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()

        systemProperty("aws.accessKeyId", "XXX")
        systemProperty("aws.secretKey", "YYY")
        systemProperty("aws.region", "us-east-1")
    }
}
