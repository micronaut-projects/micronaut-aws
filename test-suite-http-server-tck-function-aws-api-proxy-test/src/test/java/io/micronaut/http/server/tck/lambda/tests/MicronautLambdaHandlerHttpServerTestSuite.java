package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.FilterErrorTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest",
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest",
})
public class MicronautLambdaHandlerHttpServerTestSuite {
}
