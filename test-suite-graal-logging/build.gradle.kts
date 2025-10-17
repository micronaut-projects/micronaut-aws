plugins {
    id("io.micronaut.application")
}

dependencies {
    implementation(projects.micronautAwsCloudwatchLogging)
    testImplementation(mnTest.micronaut.test.junit5)
}

micronaut {
    importMicronautPlatform = false
    runtime("netty")
    testRuntime("junit5")
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
