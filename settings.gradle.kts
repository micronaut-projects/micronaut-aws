pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.build.shared.settings") version "6.3.3"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "aws-parent"

include("aws-ua")
include("aws-bom")
include("function-aws")
include("function-client-aws")
include("function-aws-api-proxy")
include("function-aws-api-proxy-test")
include("aws-secretsmanager")
include("aws-alexa")
include("aws-distributed-configuration")
include("aws-parameter-store")
include("aws-service-discovery")
include("aws-alexa-httpserver")
include("function-aws-alexa")
include("function-aws-custom-runtime")
include("aws-common")
include("aws-sdk-v1")
include("aws-sdk-v2")
include("aws-cdk")
include("aws-cloudwatch-logging")
include("aws-apigateway")
include("function-aws-test")
include("test-suite")
include("test-suite-aws-sdk-v2")
include("test-suite-http-server-tck-function-aws-api-proxy")
include("test-suite-groovy")
include("test-suite-kotlin")

configure<io.micronaut.build.MicronautBuildSettingsExtension> {
    addSnapshotRepository()
    importMicronautCatalog()
    importMicronautCatalog("micronaut-discovery-client")
    importMicronautCatalog("micronaut-groovy")
    importMicronautCatalog("micronaut-mongodb")
    importMicronautCatalog("micronaut-serde")
    importMicronautCatalog("micronaut-security")
    importMicronautCatalog("micronaut-views")
    importMicronautCatalog("micronaut-validation")
}
