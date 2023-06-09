plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
    id("io.micronaut.build.internal.aws-native-tests")
    id("io.micronaut.build.internal.aws-tests-resources")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSerde.micronaut.serde.processor)

    implementation(projects.micronautAwsSdkV2)
    implementation(libs.awssdk.s3)
    implementation(mn.micronaut.http)
    implementation(mn.micronaut.http.server.netty)
    implementation(mnValidation.micronaut.validation)
    implementation(mnSerde.micronaut.serde.jackson)
    implementation(mnLogging.logback.classic)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.client)
    testImplementation(libs.junit.jupiter.params)
}

micronaut {
    importMicronautPlatform.set(false)
    testResources {
        additionalModules.add("localstack-s3")
    }
}
