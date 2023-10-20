plugins {
    id("java-library")
    id("io.micronaut.build.internal.common")
}
dependencies {
    api(libs.aws.lambda.java.runtimeinterfaceclient)
    api(libs.managed.aws.lambda.events)
    implementation(mnTest.micronaut.test.junit5)
    implementation(projects.micronautFunctionAws)
}

spotless {
    java {
        targetExclude("**/lambda/events/**")
    }
}

tasks.withType<Checkstyle> {
    enabled = false
}
