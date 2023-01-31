plugins {
    id("groovy")
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

repositories {
    mavenCentral()
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mn.micronaut.test.spock)
    testImplementation(platform(mn.micronaut.bom))
    testImplementation(projects.functionAws)
    testImplementation(projects.functionClientAws)
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
