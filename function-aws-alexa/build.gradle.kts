plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mn.micronaut.validation)

    implementation(mn.micronaut.runtime)
    implementation(mn.micronaut.validation)

    implementation(project(":function-aws"))

    api(libs.managed.alexa.ask.sdk.lambda)
    api(project(":aws-alexa"))

    runtimeOnly(libs.jcl.over.slf4j)

    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
}
