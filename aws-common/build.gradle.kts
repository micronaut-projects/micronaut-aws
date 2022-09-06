plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    compileOnly(mn.micronaut.runtime)
    testImplementation(mn.micronaut.runtime)
}
