plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("io.micronaut.build.internal.aws-tests")
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
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

}
