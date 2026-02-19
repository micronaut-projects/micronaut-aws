plugins {
    id("io.micronaut.build.internal.java-base")
    `java-library`
}
dependencies {
    api(platform(mnTest.boms.testcontainers))
    api(libs.testcontainers.localstack)
    api(libs.testcontainers.mongodb)
}

