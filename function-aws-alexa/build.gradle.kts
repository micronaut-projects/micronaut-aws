plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    implementation(mn.micronaut.runtime)
    implementation(projects.micronautFunctionAws)
    api(libs.managed.alexa.ask.sdk.lambda)
    api(projects.micronautAwsAlexa)
    runtimeOnly(libs.jcl.over.slf4j)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.managed.alexa.ask.sdk) {
        isTransitive = false
    }
    testImplementation(libs.alexa.ask.sdk.apache.client)
}
