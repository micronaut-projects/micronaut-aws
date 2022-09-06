plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(mn.micronaut.function)
    api(libs.managed.aws.lambda.core)

    testImplementation(mn.micronaut.mongo.sync)
    testImplementation(libs.testcontainers.spock)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers)
}
