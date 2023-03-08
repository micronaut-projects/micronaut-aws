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
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(projects.micronautFunctionAws)
    testImplementation(libs.kotlin.stdlib.jdk8)
    testImplementation(projects.micronautFunctionClientAws)
    testRuntimeOnly(mn.snakeyaml)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()
    }

    named("compileTestKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "17"
            javaParameters = true
        }
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}
