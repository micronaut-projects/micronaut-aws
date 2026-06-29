plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    implementation(projects.micronautFunctionAws)
    api(libs.managed.alexa.ask.sdk.lambda) {
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }
    constraints {
        // "com.amazon.alexa:ask-sdk-lambda-support 2.86.0 requests jackson-core 2.13.0, which is affected by GHSA-72hv-8253-57qq"
        api("com.fasterxml.jackson.core:jackson-core:2.22.0") {
            because("Require a non-vulnerable jackson-core version instead of the transitive version")
        }
        // "com.amazon.alexa:ask-sdk-lambda-support 2.86.0 requests commons-io:commons-io:2.7 which is affected by GHSA-78wr-2p64-hpwj"
        api("commons-io:commons-io:2.14.0") {
            because("Require a non-vulnerable commons-io")
        }
    }
    api("com.fasterxml.jackson.core:jackson-core")
    api("commons-io:commons-io")
    api(projects.micronautAwsAlexa)
    runtimeOnly(libs.jcl.over.slf4j)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.managed.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
}
