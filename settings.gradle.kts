pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "6.7.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "aws-parent"

include("aws-alexa")
include("aws-alexa-httpserver")
include("aws-apigateway")
include("aws-bom")
include("aws-cloudwatch-logging")
include("aws-common")
include("aws-distributed-configuration")
include("aws-parameter-store")
include("aws-sdk-v1")
include("aws-sdk-v2")
include("aws-secretsmanager")
include("aws-service-discovery")
include("aws-ua")
include("aws-lambda-events-serde")
include("test-suite-aws-lambda-events")
include("test-suite-aws-lambda-events-serde")
include("test-suite-aws-lambda-events-jacksondatabind")
include("function-aws")
include("function-aws-alexa")
include("function-aws-api-proxy")
include("function-aws-api-proxy-test")
include("function-aws-custom-runtime")
include("function-aws-test")
include("function-client-aws")
include("function-client-aws-v2")

include("test-suite")
include("test-suite-aws-sdk-v2")
include("test-suite-graal")
include("test-suite-graal-logging")
include("test-suite-groovy")
include("test-suite-http-server-tck-function-aws-api-gateway-proxy-alb")
include("test-suite-http-server-tck-function-aws-api-gateway-proxy-payloadv1")
include("test-suite-http-server-tck-function-aws-api-gateway-proxy-payloadv2")
include("test-suite-http-server-tck-function-aws-api-proxy-test")
include("test-suite-kotlin")
include("test-suite-s3")

configure<io.micronaut.build.MicronautBuildSettingsExtension> {
    useStandardizedProjectNames.set(true)
    importMicronautCatalog()
    importMicronautCatalog("micronaut-discovery-client")
    importMicronautCatalog("micronaut-groovy")
    importMicronautCatalog("micronaut-mongodb")
    importMicronautCatalog("micronaut-reactor")
    importMicronautCatalog("micronaut-serde")
    importMicronautCatalog("micronaut-servlet")
    importMicronautCatalog("micronaut-security")
    importMicronautCatalog("micronaut-test-resources")
    importMicronautCatalog("micronaut-views")
    importMicronautCatalog("micronaut-validation")
}
