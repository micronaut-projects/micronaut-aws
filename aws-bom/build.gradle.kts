plugins {
    id("io.micronaut.build.internal.bom")
}

micronautBom {
    suppressions {
        acceptedLibraryRegressions.add("aws-serverless-core") // removed for 4.0.0
    }
}
