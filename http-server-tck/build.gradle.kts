plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

val micronautVersion: String by project

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    implementation(mn.micronaut.runtime)
    implementation(mn.micronaut.inject)
    implementation(mn.reactor)
    annotationProcessor(mn.micronaut.validation)
    implementation(mn.micronaut.validation)
    implementation(mn.micronaut.http.server)
    api(mn.micronaut.test.junit5)
    api("org.junit.jupiter:junit-jupiter-params")
}

java {
    sourceCompatibility = JavaVersion.toVersion("1.8")
    targetCompatibility = JavaVersion.toVersion("1.8")
}

