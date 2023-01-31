plugins {
    id("io.micronaut.build.internal.aws-module")
}

dependencies {
    annotationProcessor(libs.micronaut.graal)
    compileOnly(projects.functionAwsApiProxy)

    api(mn.micronaut.http.client)
    api(libs.managed.aws.lambda.events)
    implementation(projects.awsUa)
    testImplementation(projects.functionAwsApiProxy) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-afterburner")
    }
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
}
