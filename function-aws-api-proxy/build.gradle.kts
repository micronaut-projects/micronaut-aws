plugins {
    id("io.micronaut.build.internal.aws-module")
}
repositories {
    mavenLocal {
        mavenContent {
            snapshotsOnly()
        }
    }
}
dependencies {
    api(mn.micronaut.http.server)
    api(mn.micronaut.http.client.core)
    api(projects.micronautFunctionAws)
    api(libs.managed.aws.lambda.events)
    //api(mnServlet.micronaut.servlet.core)
    api("io.micronaut.servlet:micronaut-servlet-core:5.2.0-SNAPSHOT")
    implementation(mnReactor.micronaut.reactor)
    compileOnly(mnSecurity.micronaut.security)
    testImplementation(mn.micronaut.jackson.databind)
    testImplementation(mnViews.micronaut.views.handlebars)
}

spotless {
    java {
        targetExclude("**/io/micronaut/function/aws/proxy/QueryStringDecoder.java")
    }
}

