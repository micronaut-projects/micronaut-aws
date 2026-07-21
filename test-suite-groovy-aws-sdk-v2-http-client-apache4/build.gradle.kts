plugins {
    id("groovy")
    id("java-library")
    id("io.micronaut.build.internal.aws-tests")
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)
    testImplementation(mnTest.micronaut.test.spock)
    testImplementation(projects.micronautAwsSdkV2)
    testImplementation(libs.awssdk.apache.client)
    testImplementation(libs.awssdk.s3) {
        exclude(group = "software.amazon.awssdk", module = "apache5-client")
    }
}

tasks {
    named("test", Test::class) {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion("25")
    targetCompatibility = JavaVersion.toVersion("25")
}
