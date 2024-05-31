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
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.FluxTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest",
        "io.micronaut.http.server.tck.tests.forms.FormsSubmissionsWithListsTest",
        "io.micronaut.http.server.tck.tests.filter.options.OptionsFilterTest",
    "io.micronaut.http.server.tck.tests.LocalErrorReadingBodyTest", // Binding body different type (e.g. a String in error handler)
    "io.micronaut.http.server.tck.tests.FilterProxyTest" // Immmutable request

})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy Test")
public class MicronautLambdaHandlerHttpServerTestSuite {
}
