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
        "io.micronaut.http.server.tck.tests.hateoas.JsonErrorTest",
        "io.micronaut.http.server.tck.tests.hateoas.VndErrorTest",
        "io.micronaut.http.server.tck.tests.filter.options.OptionsFilterTest",
        "io.micronaut.http.server.tck.tests.FilterProxyTest" // Immmutable request
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Gateway Proxy v1 Event model")
public class FunctionAwsApiGatewayProxyV1HttpServerTestSuite {
}
