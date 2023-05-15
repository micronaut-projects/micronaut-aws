package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.lambda.tests"
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Gateway Proxy v1 Event model")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.MiscTest", // Bad Request 400 errors -> FORM URL encoded
    "io.micronaut.http.server.tck.tests.filter.ClientRequestFilterTest", // Multiple errors, mostly 404s
    "io.micronaut.http.server.tck.tests.filter.ClientResponseFilterTest", // body contents is not as expected
})
public class FunctionAwsApiGatewayProxyV1HttpServerTestSuite {
}
