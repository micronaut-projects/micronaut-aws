plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests-java")
    id("io.micronaut.build.internal.aws-native-tests")
}

dependencies {
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(projects.micronautFunctionAwsCustomRuntime)
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
