plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    api(projects.awsAlexa)
    implementation(mn.micronaut.http.server)
    implementation(mn.micronaut.jackson.databind)
    api(libs.managed.alexa.ask.sdk.core)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(libs.bouncycastle.provider)
    testImplementation(libs.managed.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
