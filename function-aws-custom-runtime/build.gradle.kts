plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    annotationProcessor(mn.micronaut.graal)
    compileOnly(project(":function-aws-api-proxy"))

    api(mn.micronaut.http.client)
    api(libs.managed.aws.lambda.events)

    testImplementation(project(":function-aws-api-proxy")) {
        exclude(group = "com.fasterxml.jackson.module", module = "jackson-module-afterburner")
    }
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http.server.netty)
}
