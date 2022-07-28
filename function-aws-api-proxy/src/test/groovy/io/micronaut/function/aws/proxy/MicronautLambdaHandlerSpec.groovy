package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.NonNull
import io.micronaut.function.aws.LambdaApplicationContextBuilder
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.Issue
import spock.lang.Specification

import javax.validation.constraints.NotBlank

class MicronautLambdaHandlerSpec extends Specification {
    /*
    Simple test to ensure that constructing with a builder or a fully-initialized ApplicationContext
    has no effect on the behaviour.
     */
    void "injected ApplicationContext preserves behaviour"() {
        given:
        MicronautLambdaHandler handler = new MicronautLambdaHandler(ApplicationContext.builder().properties([
                'spec.name': 'MicronautLambdaHandlerSpec'
        ]))
        ApplicationContext context = new LambdaApplicationContextBuilder()
                .properties([
                        'spec.name': 'MicronautLambdaHandlerSpec'
                ])
                .build()
                .start()
        MicronautLambdaHandler injectedHandler = new MicronautLambdaHandler(context)
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/foo', HttpMethod.GET.toString())
        builder.queryString("param", "value")

        when:
        def response = handler.handleRequest(builder.build(), new MockLambdaContext())
        def injectedResponse = injectedHandler.handleRequest(builder.build(), new MockLambdaContext())

        then:
        injectedResponse.statusCode == response.statusCode
        injectedResponse.body == "value"
        response.body == "value"
        injectedResponse.headers == response.headers

        cleanup:
        if (handler != null)
            handler.close()
        if (injectedHandler != null)
            injectedHandler.close()
    }

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/868")
    void "test selected route reflects accept header"(){
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/bar/ok', HttpMethod.GET.toString())
        builder.header("Accept", MediaType.APPLICATION_JSON)

        def response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response
        response.body == "{\"status\":\"ok\"}"

        when:
        builder = new AwsProxyRequestBuilder('/bar/ok', HttpMethod.GET.toString())
        builder.header("Accept", MediaType.TEXT_HTML)

        response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response
        response.body == "<div>ok</div>"

        cleanup:
        handler.close()
    }

    void "test behavior of 404"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/does-not-exist', HttpMethod.GET.toString())
        builder.header("Accept", MediaType.APPLICATION_JSON)

        def response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response
        response.statusCode == 404

        cleanup:
        handler.close()
    }

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1410")
    void "POST form url encoded body binding to pojo works"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        builder.body("message=World")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'
    }

    void "POST form url encoded body binding to pojo works if you don't specify body annotation"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/without-body-annotation', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        builder.body("message=World")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'
    }

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1410")
    void "form-url-encoded with Body annotation and a nested attribute"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/nested-attribute', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
        builder.body("message=World")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'

        cleanup:
        handler.close()
    }

    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1410")
    void "application-json with Body annotation and a nested attribute"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/json-nested-attribute', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body("{\"message\":\"World\"}")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'

        cleanup:
        handler.close()
    }

    void "application-json without Body annotation"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/json-without-body-annotation', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body("{\"message\":\"World\"}")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'

        cleanup:
        handler.close()
    }

    void "application-json with Body annotation and a nested attribute and Map return rendered as JSON"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/json-nested-attribute-with-map-return', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body("{\"message\":\"World\"}")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"message":"Hello World"}'

        cleanup:
        handler.close()
    }

    void "application-json with Body annotation and Object return rendered as JSON"() {
        given:
        MicronautLambdaContainerHandler handler = instantiateHandler()

        when:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/form/json-with-body-annotation-and-with-object-return', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body("{\"message\":\"World\"}")
        AwsProxyResponse response = handler.proxy(builder.build(), new MockLambdaContext())

        then:
        response.statusCode == 200
        response.body == '{"greeting":"Hello World"}'

        cleanup:
        handler.close()
    }

    @Controller
    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerSpec')
    @Secured(SecurityRule.IS_ANONYMOUS)
    static class SimpleController {
        @Get(uri = "/foo")
        HttpResponse<String> getParamValue(HttpRequest request) {
            return HttpResponse.ok()
                    .body(request.getParameters().get("param"))
                    .header("foo", "bar")
        }
    }

    @Controller("/bar")
    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerSpec')
    @Secured(SecurityRule.IS_ANONYMOUS)
    static class ProduceController {
        @Get(value = "/ok", produces = MediaType.APPLICATION_JSON)
        String getOkAsJson() {
            return "{\"status\":\"ok\"}"
        }

        @Get(value = "/ok", produces = MediaType.TEXT_HTML)
        String getOkAsHtml() {
            return "<div>ok</div>"
        }
    }

    @Introspected
    static class MessageCreate {

        @NonNull
        @NotBlank
        private final String message;

        MessageCreate(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        String getMessage() {
            return message;
        }
    }

    @Introspected
    static class MyResponse {

        @NonNull
        @NotBlank
        private final String greeting;

        MyResponse(@NonNull String greeting) {
            this.greeting = greeting
        }

        @NonNull
        String getGreeting() {
            return greeting
        }
    }

    @Controller("/form")
    @Requires(property = 'spec.name', value = 'MicronautLambdaHandlerSpec')
    @Secured(SecurityRule.IS_ANONYMOUS)
    static class FormController {

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/without-body-annotation")
        String withoutBodyAnnotation(MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.getMessage()}\"}";
        }

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post
        String save(@Body MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.getMessage()}\"}";
        }

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/nested-attribute")
        String save(@Body("message") String value) {
            "{\"message\":\"Hello ${value}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-without-body-annotation")
        String jsonWithoutBody(MessageCreate messageCreate) {
            "{\"message\":\"Hello ${messageCreate.message}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-nested-attribute")
        String jsonNestedAttribute(@Body("message") String value) {
            "{\"message\":\"Hello ${value}\"}";
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-nested-attribute-with-map-return")
        Map<String, String> jsonNestedAttributeWithMapReturn(@Body("message") String value) {
            [message: "Hello ${value}".toString()]
        }

        @Consumes(MediaType.APPLICATION_JSON)
        @Post("/json-with-body-annotation-and-with-object-return")
        MyResponse jsonNestedAttributeWithObjectReturn(@Body MessageCreate messageCreate) {
            new MyResponse("Hello ${messageCreate.message}")
        }
    }

    private static MicronautLambdaContainerHandler instantiateHandler() {
        new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties([
                        'spec.name'                 : 'MicronautLambdaHandlerSpec',
                        'micronaut.security.enabled': false
                ])
        )
    }
}
