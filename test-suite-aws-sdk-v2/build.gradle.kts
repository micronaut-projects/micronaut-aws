plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

repositories {
    mavenCentral()
}

val micronautVersion: String by project

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(platform(mn.micronaut.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mn.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(projects.awsSdkV2)
}
tasks {
    named("test", Test::class) {
        useJUnitPlatform()
    }
}
java {
    sourceCompatibility = JavaVersion.toVersion("1.8")
    targetCompatibility = JavaVersion.toVersion("1.8")
}
