plugins {
    id("io.micronaut.build.internal.docs")
    id("io.micronaut.build.internal.dependency-updates")
    id("io.micronaut.build.internal.quality-reporting")
}

repositories {
    mavenCentral()
}
configurations.all {
    resolutionStrategy {
        preferProjectModules()
    }
}
