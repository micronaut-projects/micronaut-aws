plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("io.micronaut.build.internal.aws-tests")

}

repositories {
    mavenCentral()
}

val micronautVersion: String by project

dependencies {
    kaptTest(mn.micronaut.inject.java)
    testAnnotationProcessor(platform(mn.micronaut.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mn.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(projects.functionAws)
    testImplementation(libs.kotlin.stdlib.jdk8)
    testImplementation(projects.functionClientAws)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()
    }

    named("compileTestKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "1.8"
            javaParameters = true
        }
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("1.8")
    targetCompatibility = JavaVersion.toVersion("1.8")
}
