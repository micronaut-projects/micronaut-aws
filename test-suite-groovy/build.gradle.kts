plugins {
    id("groovy")
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.spock)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(projects.micronautFunctionAws)
    testImplementation(projects.micronautFunctionClientAwsV2)
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
