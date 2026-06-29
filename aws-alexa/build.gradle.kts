plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    implementation(mn.micronaut.jackson.databind)
    compileOnly(libs.managed.alexa.ask.sdk)
    api(libs.managed.alexa.ask.sdk.core) {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }
    constraints {
        // " com.amazon.alexa:ask-sdk-core 2.86.0 requests jackson-core 2.13.0, which is affected by GHSA-72hv-8253-57qq"
        api("com.fasterxml.jackson.core:jackson-core:2.22.0") {
            because("Require a non-vulnerable jackson-core version instead of the transitive version")
        }
    }
    api("com.fasterxml.jackson.core:jackson-core")
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.managed.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.groovy.json)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
