plugins {
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

repositories {
    mavenCentral()
}

val micronautVersion: String by project

dependencies {
    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionClientAws)
    testRuntimeOnly(mn.snakeyaml)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()

        systemProperty("aws.accessKeyId", "XXX")
        systemProperty("aws.secretKey", "YYY")
        systemProperty("aws.region", "us-east-1")
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}
