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
@SuiteDisplayName("HTTP Server TCK for Function AWS API Gateway Proxy v2 Event model")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.MiscTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Multiple routes are selected
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // 503 a service isn't running (assume it's an AWS service)
    "io.micronaut.http.server.tck.tests.filter.ClientRequestFilterTest", // Multiple errors, mostly 404s
    "io.micronaut.http.server.tck.tests.filter.ClientResponseFilterTest", // body contents is not as expected
})
public class FunctionAwsApiGatewayProxyV2HttpServerTestSuite {
}
