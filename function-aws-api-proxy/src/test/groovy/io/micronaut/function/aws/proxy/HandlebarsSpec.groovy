package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.CollectionUtils
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.views.ModelAndView
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class HandlebarsSpec extends Specification {

    @Shared
    @AutoCleanup
    MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
            ApplicationContext.builder().properties(
                    'micronaut.security.enabled': false,
                    'spec.name': 'HandlebarsSpec'
            )
    )
    @Shared
    Context lambdaContext = new MockLambdaContext()

    void "test handlebars view rendering"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/views/render-html', HttpMethod.GET.toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 200
        response.body == '<html>Hello Luke Skywalker</html>'
    }

    @Controller('/views')
    @Requires(property = 'spec.name', value = 'HandlebarsSpec')
    static class BodyController {

        @Get("/render-html")
        @Produces(MediaType.TEXT_HTML)
        ModelAndView getHtmlFromRenderEngine(Context context) {
            ModelAndView mav = new ModelAndView("home", CollectionUtils.mapOf("firstname", "Luke", "lastname", "Skywalker"));
            mav;
        }
    }
}
