plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
    id("io.micronaut.build.internal.common")
}
dependencies {
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionClientAwsV2)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()

        systemProperty("aws.accessKeyId", "XXX")
        systemProperty("aws.secretKey", "YYY")
        systemProperty("aws.region", "us-east-1")
    }
}

spotless {
    java {
        targetExclude("**/docs/**")
    }
}

tasks.withType<Checkstyle> {
    enabled = false
}
