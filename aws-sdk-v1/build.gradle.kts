plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    api(platform(libs.boms.aws.java.sdk.v1))
    api(libs.managed.aws.java.sdk.core) {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }
    constraints {
        // "com.amazonaws:aws-java-sdk-core 1.12.797 requests jackson-core 2.17.2, which is affected by GHSA-72hv-8253-57qq"
        api("com.fasterxml.jackson.core:jackson-core:2.18.6") {
            because("Require a non-vulnerable jackson-core version instead of the transitive version")
        }
    }
    api("com.fasterxml.jackson.core:jackson-core")
    api(projects.micronautAwsCommon)
    runtimeOnly(libs.jcl.over.slf4j)
    testImplementation(mn.micronaut.http.server.netty)
    testRuntimeOnly(mn.snakeyaml)
}
