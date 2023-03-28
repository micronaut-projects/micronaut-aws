plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    api(platform(libs.boms.aws.java.sdk.v2))
    api(libs.awssdk.dynamodb)

    testAnnotationProcessor(platform(mn.micronaut.core.bom))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(libs.junit.jupiter.api)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
