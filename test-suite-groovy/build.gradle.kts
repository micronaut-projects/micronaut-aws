plugins {
    id("groovy")
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
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
    testImplementation(projects.functionAws)
    testImplementation(projects.functionClientAws)
    testRuntimeOnly(mn.snakeyaml)
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}
