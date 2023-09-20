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
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.HeadersTest", // https://github.com/micronaut-projects/micronaut-aws/issues/1861
    "io.micronaut.http.server.tck.tests.FilterProxyTest" // Immmutable request
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Gateway Proxy v2 Event model")
public class FunctionAwsApiGatewayProxyV2HttpServerTestSuite {
}
