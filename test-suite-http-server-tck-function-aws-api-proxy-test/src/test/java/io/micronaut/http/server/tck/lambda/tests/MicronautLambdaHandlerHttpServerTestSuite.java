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
    "io.micronaut.http.server.tck.tests.forms.UploadTest",

    "io.micronaut.http.server.tck.tests.VersionTest",
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest",
    "io.micronaut.http.server.tck.tests.StreamTest",
    "io.micronaut.http.server.tck.tests.StatusTest",
    "io.micronaut.http.server.tck.tests.RequestUriContainsQueryValueTest",
    "io.micronaut.http.server.tck.tests.OctetTest",
    "io.micronaut.http.server.tck.tests.filter.options.OptionsFilterTest",
    "io.micronaut.http.server.tck.tests.forms.FormsInputNumberOptionalTest",
    "io.micronaut.http.server.tck.tests.bodywritable.HtmlBodyWritableTest",
    "io.micronaut.http.server.tck.tests.RequestUriTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerFluxTest",
    "io.micronaut.http.server.tck.tests.BodyArgumentTest",
    "io.micronaut.http.server.tck.tests.codec.JsonCodecAdditionalType2Test",
    "io.micronaut.http.server.tck.tests.ResponseStatusTest",
    "io.micronaut.http.server.tck.tests.ErrorNotFoundRouteErrorRouteTest",
    "io.micronaut.http.server.tck.tests.LocalErrorReadingBodyTest",
    "io.micronaut.http.server.tck.tests.routing.RootRoutingTest",
    "io.micronaut.http.server.tck.tests.ParameterTest",
    "io.micronaut.http.server.tck.tests.forms.FormBindingUsingMethodParametersTest",
    "io.micronaut.http.server.tck.tests.forms.FormsSubmissionsWithListsTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest",
    "io.micronaut.http.server.tck.tests.NoBodyResponseTest",
    "io.micronaut.http.server.tck.tests.filter.HttpServerFilterExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.HelloWorldTest",
    "io.micronaut.http.server.tck.tests.FluxTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest",
    "io.micronaut.http.server.tck.tests.MiscTest",
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.forms.FormUrlEncodedBodyInRequestFilterTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterCompletionStageFutureProceedTest",
    "io.micronaut.http.server.tck.tests.cors.CorsSimpleRequestTest",
    "io.micronaut.http.server.tck.tests.DeleteWithoutBodyTest",
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest",
    "io.micronaut.http.server.tck.tests.CookiesTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerStringTest",
    "io.micronaut.http.server.tck.tests.textplain.TxtPlainBooleanTest",
    "io.micronaut.http.server.tck.tests.ConsumesTest",
    "io.micronaut.http.server.tck.tests.RemoteAddressTest",
    "io.micronaut.http.server.tck.tests.mediatype.StringDefaultMediaTypeTest",
    "io.micronaut.http.server.tck.tests.codec.JsonCodecAdditionalTypeAutomaticTest",
    "io.micronaut.http.server.tck.tests.filter.HttpServerFilterTest",
    "io.micronaut.http.server.tck.tests.binding.LocalDateTimeTest",
    "io.micronaut.http.server.tck.tests.ErrorNotFoundRouteExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.FiltersTest",
    "io.micronaut.http.server.tck.tests.MissingBodyAnnotationTest",
    "io.micronaut.http.server.tck.tests.cors.CorsDisabledByDefaultTest",
    "io.micronaut.http.server.tck.tests.forms.FormsJacksonAnnotationsTest",
    "io.micronaut.http.server.tck.tests.jsonview.JsonViewsTest",
    "io.micronaut.http.server.tck.tests.HeadersTest",
    "io.micronaut.http.server.tck.tests.filter.RequestFilterCompletableFutureFutureProceedTest",
    "io.micronaut.http.server.tck.tests.textplain.TxtPlainBigDecimalTest",
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthResultTest",
    "io.micronaut.http.server.tck.tests.NotFoundExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.ExpressionTest",
    "io.micronaut.http.server.tck.tests.PublisherExceptionHandlerTest",
    "io.micronaut.http.server.tck.tests.FilterErrorTest",
    "io.micronaut.http.server.tck.tests.codec.JsonCodecAdditionalTypeTest",
    "io.micronaut.http.server.tck.tests.exceptions.HtmlErrorPageTest",
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest",
})
@SuiteDisplayName("HTTP Server TCK for Function AWS API Proxy Test")
public class MicronautLambdaHandlerHttpServerTestSuite {
}
