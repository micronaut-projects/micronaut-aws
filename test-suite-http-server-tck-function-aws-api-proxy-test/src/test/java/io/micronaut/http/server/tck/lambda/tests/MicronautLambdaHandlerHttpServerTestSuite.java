package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyArgumentTest",
    "io.micronaut.http.server.tck.tests.ResponseStatusTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest",
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest",
    "io.micronaut.http.server.tck.tests.FilterErrorTest",
    "io.micronaut.http.server.tck.tests.ConsumesTest"
})
public class MicronautLambdaHandlerHttpServerTestSuite {
}
