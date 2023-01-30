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
    kaptTest("io.micronaut:micronaut-inject-java:$micronautVersion")

    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(projects.functionAws)
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")
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
