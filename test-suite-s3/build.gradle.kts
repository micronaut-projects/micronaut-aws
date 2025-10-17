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
    testImplementation(mnTest.junit.jupiter.params)
}

micronaut {
    importMicronautPlatform.set(false)
    testResources {
        additionalModules.add("localstack-s3")
    }
}

graalvmNative {
    binaries {
        all {
            buildArgs.add("--trace-class-initialization=org.slf4j.LoggerFactory")

            buildArgs.add("--initialize-at-build-time=org.junit.platform.engine.support.store.NamespacedHierarchicalStore\$EvaluatedValue")
            buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.LauncherPhase")
            buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.DiscoveryIssueNotifier")
            buildArgs.add("--initialize-at-build-time=org.junit.platform.launcher.core.HierarchicalOutputDirectoryCreator")
        }
    }
}
