plugins {
    id("io.micronaut.build.internal.module")
}

val micronautVersion: String by project

dependencies {
    api(libs.aws.cdk.lib)
    api("io.micronaut.starter:micronaut-starter-api:$micronautVersion")
    testImplementation(project(":function-aws-api-proxy"))
}
