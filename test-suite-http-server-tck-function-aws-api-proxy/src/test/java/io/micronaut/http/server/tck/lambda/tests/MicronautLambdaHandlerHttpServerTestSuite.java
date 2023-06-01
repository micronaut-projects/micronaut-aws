package io.micronaut.http.server.tck.lambda.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy")
@ExcludeClassNamePatterns(value = {
    "io.micronaut.http.server.tck.tests.RemoteAddressTest",
    "io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest",
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.OctetTest",
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest",
    "io.micronaut.http.server.tck.tests.staticresources.StaticResourceTest",
    "io.micronaut.http.server.tck.tests.cors.CrossOriginTest",
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest"
})
public class MicronautLambdaHandlerHttpServerTestSuite {
}
