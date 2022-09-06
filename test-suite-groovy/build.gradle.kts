plugins {
    id("groovy")
    id("java-library")
}

repositories {
    mavenCentral()
}

val micronautVersion: String by project
val micronautTestVersion: String by project
val spockVersion: String by project

dependencies {
    testCompileOnly("io.micronaut:micronaut-inject-groovy:$micronautVersion")
    testImplementation("org.spockframework:spock-core:${spockVersion}") {
        exclude(module = "groovy-all")
    }
    testImplementation("io.micronaut.test:micronaut-test-spock:$micronautTestVersion")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation(project(":function-aws"))
    testImplementation(project(":function-client-aws"))
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
