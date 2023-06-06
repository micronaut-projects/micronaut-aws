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
        }
    }
}
