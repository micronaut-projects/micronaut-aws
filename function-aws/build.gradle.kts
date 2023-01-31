plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.function)
    api(libs.managed.aws.lambda.core)
    testImplementation(mnMongo.micronaut.mongo.sync)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.spock)
    testImplementation(libs.testcontainers.mongodb)
    testImplementation(libs.testcontainers)
    testRuntimeOnly(mn.micronaut.jackson.databind)
}
