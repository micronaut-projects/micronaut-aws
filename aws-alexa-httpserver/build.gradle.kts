plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mn.micronaut.validation)

    implementation(mn.micronaut.validation)

    api(project(":aws-alexa"))

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
