plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mn.micronaut.validation)
    implementation(mn.micronaut.validation)
    api(projects.awsAlexa)
    implementation(mn.micronaut.http.server)
    api(libs.managed.alexa.ask.sdk.core)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(libs.bouncycastle.provider)
    testImplementation(libs.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
