plugins {
    id("io.micronaut.build.internal.parent")
    id("io.micronaut.build.internal.dependency-updates")
}

repositories {
    mavenLocal {
        content {
            includeGroup("io.micronaut")
        }
    }
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        name = "Central Portal Snapshots"
        mavenContent {
            snapshotsOnly()
        }
        content {
            includeGroupByRegex("io\\.micronaut(\\..+)?")
        }
    }
    mavenCentral()
}
configurations.all {
    resolutionStrategy {
        preferProjectModules()
    }
}
