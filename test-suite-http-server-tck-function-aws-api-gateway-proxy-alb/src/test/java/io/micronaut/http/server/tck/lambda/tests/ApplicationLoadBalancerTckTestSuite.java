package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.*;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.lambda.tests"
})
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.HeadersTest", // https://github.com/micronaut-projects/micronaut-aws/issues/1861
    "io.micronaut.http.server.tck.tests.FilterProxyTest" // Immmutable request
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Gateway Proxy Application Load Balancer Event")
public class ApplicationLoadBalancerTckTestSuite {
}
