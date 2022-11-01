package io.micronaut.aws.alexa.locale

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Request
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.LocaleResolver
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "alexa.locale-resolution.fixed", value = "es-ES")
@MicronautTest(startApplication = false)
class HandlerInputFixedLocaleResolverSpec extends Specification {

    @Inject
    BeanContext beanContext

    @Inject
    LocaleResolver<HandlerInput> localeResolver

    void "fixed locale takes precedence"() {
        given:
        def request = Stub(Request) {
            getLocale() >> "en-US"
        }
        def input = Stub(HandlerInput) {
            getRequest() >> request
        }

        expect:
        beanContext.containsBean(HandlerInputFixedLocaleResolver)

        when:
        Locale locale = localeResolver.resolveOrDefault(input)

        then:
        new Locale("es", "ES") == locale
    }
}
