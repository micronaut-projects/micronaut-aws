plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.7.20")
    id("org.jetbrains.kotlin.kapt") version ("1.7.20")
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
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22")
    testImplementation(projects.functionClientAws)
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
