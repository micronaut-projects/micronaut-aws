plugins {
    id("io.micronaut.build.internal.module")
}

dependencies {
    api(platform(libs.boms.aws.java.sdk.v2))
    api(project(":aws-common"))

    compileOnly(libs.graal)

    // Clients
    compileOnly(libs.awssdk.url.connection.client)
    compileOnly(libs.awssdk.netty.nio.client)
    compileOnly(libs.awssdk.apache.client)

    // Services
    compileOnly(libs.awssdk.apigatewaymanagementapi)
    compileOnly(libs.awssdk.s3)
    compileOnly(libs.awssdk.dynamodb)
    compileOnly(libs.awssdk.ses)
    compileOnly(libs.awssdk.sns)
    compileOnly(libs.awssdk.sqs)
    compileOnly(libs.awssdk.ssm)
    compileOnly(libs.awssdk.secretsmanager)
    compileOnly(libs.awssdk.servicediscovery)

    // Tests
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.awssdk.apigatewaymanagementapi)
    testImplementation(libs.awssdk.servicediscovery)
    testImplementation(libs.awssdk.url.connection.client)
    testImplementation(libs.awssdk.netty.nio.client)
    testImplementation(libs.awssdk.apache.client)
    testImplementation(libs.awssdk.s3)
    testImplementation(libs.awssdk.dynamodb)
    testImplementation(libs.awssdk.ses)
    testImplementation(libs.awssdk.secretsmanager)
    testImplementation(libs.awssdk.sns)
    testImplementation(libs.awssdk.sqs)
    testImplementation(libs.awssdk.ssm)
    testImplementation(libs.awssdk.rekognition)
    testRuntimeOnly(libs.jcl.over.slf4j)
}
