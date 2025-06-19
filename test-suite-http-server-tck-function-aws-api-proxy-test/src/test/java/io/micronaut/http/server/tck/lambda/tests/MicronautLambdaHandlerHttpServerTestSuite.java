package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.*;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.lambda.tests"
})
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.cors.CrossOriginTest", // CrossOriginTest#httHeaderValueAccessControlExposeHeaderValueCanBeSetViaCrossOriginAnnotation fails
    "io.micronaut.http.server.tck.tests.FilterProxyTest", // Immmutable request
    "io.micronaut.http.server.tck.tests.filter.CacheControlTest",
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy Test")
public class MicronautLambdaHandlerHttpServerTestSuite {
}
