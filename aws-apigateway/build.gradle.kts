plugins {
    id("io.micronaut.build.internal.aws-module")
}
dependencies {
    compileOnly(mn.micronaut.http)
    compileOnly(projects.micronautFunctionAwsApiProxy)
    compileOnly(libs.managed.aws.lambda.events)
    testImplementation(projects.micronautFunctionAwsApiProxy)
}
