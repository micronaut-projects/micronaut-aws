plugins {
    id("io.micronaut.build.internal.bom")
}

micronautBom {
    suppressions {
        acceptedVersionRegressions.add("aws-serverless-core") // removed for 4.0.0
        acceptedLibraryRegressions.add("aws-serverless-core") // removed for 4.0.0
        acceptedVersionRegressions.add("aws-cdk-lib") // moved to starter for 4.0.0
        acceptedLibraryRegressions.add("micronaut-aws-cdk") // moved to starter for 4.0.0
    }
}
