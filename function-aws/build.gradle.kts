plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(mn.micronaut.function)
    api(libs.managed.aws.lambda.core)
    implementation(mn.micronaut.json.core)
    testImplementation(mnMongo.micronaut.mongo.sync)
    testImplementation(mnTestResources.testcontainers.core)
    testImplementation(mnTestResources.testcontainers.mongodb)
    testImplementation(projects.micronautAwsLambdaEventsSerde)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
