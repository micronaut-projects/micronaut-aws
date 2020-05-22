package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class CookiesSpec extends Specification {
    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.build().properties([
                    'spec.name': 'CookiesSpec'
            ])
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test no cookies"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/cookies-test/all', HttpMethod.GET.toString())

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '{}'
    }

    void "test getCookies method"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/cookies-test/all', HttpMethod.GET.toString())
        builder.cookie("one", "foo")
        builder.cookie("two", "bar")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '{"one":"foo","two":"bar"}'
    }

    void "test Cookie bind"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/cookies-test/bind', HttpMethod.GET.toString())
        builder.cookie("one", "foo")
        builder.cookie("two", "bar")

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '{"one":"foo","two":"bar"}'
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Controller('/cookies-test')
    @Requires(property = 'spec.name', value = 'CookiesSpec')
    static class CookieController {

        @Get(uri = "/all")
        Map<String, String> all(HttpRequest request) {
            Map<String, String> map = [:]
            for (entry in request.cookies) {
                map.put(entry.key, entry.value.value)
            }
            return map
        }

        @Get(uri = "/bind")
        Map<String, String> all(@CookieValue String one, @CookieValue String two) {
            [
                    one: one,
                    two: two
            ]
        }
    }
}
