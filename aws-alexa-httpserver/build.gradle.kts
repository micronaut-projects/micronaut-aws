plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    api(projects.micronautAwsAlexa)
    implementation(mn.micronaut.http.server)
    implementation(mn.micronaut.jackson.databind)
    api(libs.managed.alexa.ask.sdk.core) {
        exclude(group = "com.fasterxml.jackson.core", module = "jackson-core")
    }
    constraints {
        // " com.amazon.alexa:ask-sdk-core 2.86.0 requests jackson-core 2.13.0, which is affected by several CVEs"
        api("com.fasterxml.jackson.core:jackson-core:2.22.2") {
            because("Require a non-vulnerable jackson-core version instead of the transitive version")
        }
    }
    api("com.fasterxml.jackson.core:jackson-core")
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(libs.bouncycastle.provider)
    testImplementation(libs.managed.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
