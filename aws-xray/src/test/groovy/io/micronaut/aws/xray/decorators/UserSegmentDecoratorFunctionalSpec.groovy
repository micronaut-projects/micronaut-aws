package io.micronaut.aws.xray.decorators

import io.micronaut.aws.xray.TestEmitter
import io.micronaut.aws.xray.TestEmitterXRayRecorderBuilderBeanListener
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.rules.SecurityRule
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import spock.lang.Specification
import jakarta.inject.Singleton
import java.security.Principal

@Property(name = "spec.name", value = "UserSegmentDecoratorFunctionalSpec")
@MicronautTest
class UserSegmentDecoratorFunctionalSpec extends Specification {
    @Inject
    @Client("/")
    HttpClient httpClient

    @Inject
    TestEmitter emitter

    void "Principal is added to the segment"() {
        given:
        BlockingHttpClient client = httpClient.toBlocking()
        HttpRequest<?> request = HttpRequest.GET('/echo/username')
                .basicAuth("sherlock", "elementary")

        when:
        client.exchange(request)

        then:
        noExceptionThrown()
        emitter.segments
        emitter.segments.size() == 1
        emitter.segments.get(0).user == 'sherlock'
    }

    @Requires(property = 'spec.name', value = 'UserSegmentDecoratorFunctionalSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'UserSegmentDecoratorFunctionalSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {
    }


    @Requires(property = "spec.name", value = "UserSegmentDecoratorFunctionalSpec")
    @Controller("/echo")
    static class MockController {
        @Secured(SecurityRule.IS_AUTHENTICATED)
        @Produces(MediaType.TEXT_PLAIN)
        @Get("/username")
        String index(Principal principal) {
            principal.name
        }
    }

    @Requires(property = "spec.name", value = "UserSegmentDecoratorFunctionalSpec")
    @Singleton
    static class MockAuthenticationProvider implements AuthenticationProvider {

        @Override
        Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
            Mono.just(AuthenticationResponse.success("sherlock"))
        }
    }
}
