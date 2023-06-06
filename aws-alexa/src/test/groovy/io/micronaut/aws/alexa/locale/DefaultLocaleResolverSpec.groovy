package io.micronaut.aws.alexa.locale

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Request
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.LocaleResolver
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class DefaultLocaleResolverSpec extends Specification {

    @Inject
    BeanContext beanContext

    @Inject
    LocaleResolver<HandlerInput> localeResolver

    void "default locale is en-US"() {
        given:
        def request = Stub(Request) {
            getLocale() >> ""
        }
        def input = Stub(HandlerInput) {
            getRequest() >> request
        }
        when:
        Locale locale = localeResolver.resolveOrDefault(input)

        then:
        new Locale("en", "US") == locale
    }
}
