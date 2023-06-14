plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    compileOnly(mn.micronaut.http)
    compileOnly(projects.micronautFunctionAwsApiProxy)
    compileOnly(projects.micronautAwsLambdaEvents)
    compileOnly(libs.managed.aws.lambda.events)
    testImplementation(projects.micronautAwsLambdaEvents)
    testImplementation(projects.micronautFunctionAwsApiProxy)
    testImplementation(libs.managed.aws.lambda.events)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
