plugins {
    id("io.micronaut.build.internal.parent")
    id("io.micronaut.build.internal.dependency-updates")
}

repositories {
    mavenCentral()
}
configurations.all {
    resolutionStrategy {
        preferProjectModules()
    }
}
