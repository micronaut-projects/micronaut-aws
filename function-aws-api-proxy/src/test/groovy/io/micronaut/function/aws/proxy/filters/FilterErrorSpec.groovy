package io.micronaut.function.aws.proxy.filters

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.core.util.StringUtils
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Status
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.web.router.MethodBasedRouteMatch
import io.micronaut.web.router.RouteMatch
import org.reactivestreams.Publisher
import spock.lang.Ignore
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class FilterErrorSpec extends Specification {

    final static SPEC_NAME = FilterErrorSpec.simpleName

    private Context lambdaContext = new MockLambdaContext()

    private static createHandler(Map props) {
        new MicronautLambdaContainerHandler(
                ApplicationContext.builder().properties(props)
        )
    }

    private static requestBuilder(String path, HttpMethod method) {
        new AwsProxyRequestBuilder(path, method.toString())
    }

    void 'test errors emitted from filters interacting with exception handlers'() {
        given:
        def handler = createHandler(['spec.name': SPEC_NAME])
        def context = handler.applicationContext
        def builder = requestBuilder('/filter-error-spec', HttpMethod.GET)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        and:
        First first = context.getBean(First)
        Next next = context.getBean(Next)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body == 'from filter exception handler'

        and:
        first.executedCount.get() == 1
        first.responseStatus.getAndSet(null) == null
        next.executedCount.get() == 0
    }

    void 'test errors emitted from second filter interacting with exception handlers'() {
        given:
        def handler = createHandler(['spec.name': SPEC_NAME])
        def context = handler.applicationContext
        def builder = requestBuilder('/filter-error-spec', HttpMethod.GET)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header('X-Passthru', 'true')

        and:
        First first = context.getBean(First)
        Next next = context.getBean(Next)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body == 'from NEXT filter exception handler'

        and:
        first.executedCount.get() == 1
        first.responseStatus.getAndSet(null) == HttpStatus.BAD_REQUEST
        next.executedCount.get() == 1
    }

    void 'test non once per request filter throwing error does not loop'() {
        given:
        def handler = createHandler(['spec.name': SPEC_NAME + '2'])
        def context = handler.applicationContext
        def builder = requestBuilder('/filter-error-spec', HttpMethod.GET)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        and:
        FirstEvery filter = context.getBean(FirstEvery)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST.code
        response.body == 'from filter exception handler'
        filter.executedCount.get() == 1
    }

    void 'test filter throwing exception handled by exception handler throwing exception'() {
        given:
        def handler = createHandler([
                'spec.name': SPEC_NAME + '3',
                'micronaut.security.enabled': false,
        ])
        def context = handler.applicationContext
        def builder = requestBuilder('/filter-error-spec-3', HttpMethod.GET)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        and:
        ExceptionException filter = context.getBean(ExceptionException)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR.code
        response.body.contains('from exception handler')
        filter.executedCount.get() == 1
        filter.responseStatus.getAndSet(null) == HttpStatus.INTERNAL_SERVER_ERROR
    }

    void "test the error route is the route match"() {
        given:
        def handler = createHandler([
                'spec.name': SPEC_NAME + '4',
                'micronaut.security.enabled': false,
        ])
        def context = handler.applicationContext
        def builder = requestBuilder('/filter-error-spec-4/status', HttpMethod.GET)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        and:
        ExceptionRoute filter = context.getBean(ExceptionRoute)

        when:
        def response = handler.proxy(builder.build(), lambdaContext)
        def match = filter.routeMatch.getAndSet(null)

        then:
        response.statusCode == HttpStatus.OK.code
        match instanceof MethodBasedRouteMatch
        ((MethodBasedRouteMatch) match).getName() == 'testStatus'

//        when:
//        HttpResponse<String> response = Flux.from(client.exchange("/filter-error-spec-4/exception", String))
//                .blockFirst()
//        def match = filter.routeMatch.getAndSet(null)
//
//        then:
//        response.status() == HttpStatus.OK
//        match instanceof MethodBasedRouteMatch
//        ((MethodBasedRouteMatch) match).getName() == "testException"
    }

    @Requires(property = 'spec.name', value = 'FilterErrorSpec')
    @Filter(Filter.MATCH_ALL_PATTERN)
    static class First implements HttpServerFilter {
        AtomicInteger executedCount = new AtomicInteger(0)
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>()

        private void setResponse(MutableHttpResponse<?> r) {
            responseStatus.set(r.status())
        }

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            executedCount.incrementAndGet()
            if (StringUtils.isTrue(request.getHeaders().get("X-Passthru"))) {
                return Publishers.then(chain.proceed(request), this::setResponse)
            }
            return Publishers.just(new FilterException())
        }

        @Override
        int getOrder() {
            10
        }
    }

    @Requires(property = 'spec.name', value = 'FilterErrorSpec')
    @Filter(Filter.MATCH_ALL_PATTERN)
    static class Next implements HttpServerFilter {
        AtomicInteger executedCount = new AtomicInteger(0)

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            executedCount.incrementAndGet()
            return Publishers.just(new NextFilterException())
        }

        @Override
        int getOrder() {
            20
        }
    }

    @Requires(property = 'spec.name', value = 'FilterErrorSpec2')
    @Filter(Filter.MATCH_ALL_PATTERN)
    static class FirstEvery implements HttpServerFilter {
        AtomicInteger executedCount = new AtomicInteger(0)

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            executedCount.incrementAndGet()
            return Publishers.just(new FilterException())
        }

        @Override
        int getOrder() {
            10
        }
    }

    @Requires(property = 'spec.name', value = 'FilterErrorSpec3')
    @Filter(Filter.MATCH_ALL_PATTERN)
    static class ExceptionException implements HttpServerFilter {
        AtomicInteger executedCount = new AtomicInteger(0)
        AtomicReference<HttpStatus> responseStatus = new AtomicReference<>()

        private void setResponse(MutableHttpResponse<?> r) {
            responseStatus.set(r.status())
        }

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            executedCount.incrementAndGet()
            return Publishers.then(chain.proceed(request), this::setResponse)
        }

        @Override
        int getOrder() {
            10
        }
    }

    @Requires(property = 'spec.name', value = 'FilterErrorSpec4')
    @Filter(Filter.MATCH_ALL_PATTERN)
    static class ExceptionRoute implements HttpServerFilter {
        AtomicReference<RouteMatch<?>> routeMatch = new AtomicReference<>()

        @Override
        Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
            return Publishers.then(chain.proceed(request), { resp ->
                routeMatch.set(resp.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch).get())
            })
        }

        @Override
        int getOrder() {
            10
        }
    }

    @Controller("/filter-error-spec")
    static class NeverReachedController {
        @Get
        String get() {
            return "OK"
        }
    }

    @Controller("/filter-error-spec-3")
    static class HandledByHandlerController {
        @Get
        String get() {
            throw new FilterExceptionException()
        }
    }

    @Controller("/filter-error-spec-4")
    static class HandledByErrorRouteController {
        @Get("/exception")
        String getException() {
            throw new FilterExceptionException()
        }

        @Get("/status")
        HttpStatus getStatus() {
            return HttpStatus.NOT_FOUND
        }

        @Error(exception = FilterExceptionException)
        @Status(HttpStatus.OK)
        void testException() {}

        @Error(status = HttpStatus.NOT_FOUND)
        @Status(HttpStatus.OK)
        void testStatus() {}
    }

}
