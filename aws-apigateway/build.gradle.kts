plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    compileOnly(mn.micronaut.http)
    compileOnly(projects.functionAwsApiProxy)
    compileOnly(libs.managed.aws.lambda.events)
    testImplementation(projects.functionAwsApiProxy)
    testImplementation(libs.managed.aws.lambda.events)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
