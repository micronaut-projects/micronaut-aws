plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mn.micronaut.validation)
    implementation(mn.micronaut.validation)
    compileOnly(libs.alexa.ask.sdk)
    api(libs.managed.alexa.ask.sdk.core)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.groovy.json)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
