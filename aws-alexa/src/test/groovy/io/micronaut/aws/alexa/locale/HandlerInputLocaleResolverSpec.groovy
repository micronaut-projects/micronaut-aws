package io.micronaut.aws.alexa.locale

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Request
import io.micronaut.core.util.LocaleResolver
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest(startApplication = false)
class HandlerInputLocaleResolverSpec extends Specification {

    @Inject
    LocaleResolver<HandlerInput> localeResolver

    @Unroll
    void "resolve locale from handler input"(String languageCode, Locale expected) {
        given:
        def request = Stub(Request) {
            getLocale() >> languageCode
        }
        def input = Stub(HandlerInput) {
            getRequest() >> request
        }

        when:
        Locale locale = localeResolver.resolveOrDefault(input)

        then:
        expected == locale

        where:
        languageCode | expected
        'ar-SA'      | new Locale("ar", "SA")
        'de-DE'      | Locale.GERMANY
        'en-AU'      | new Locale("en", "AU")
        'en-CA'      | Locale.CANADA
        'en-GB'      | Locale.UK
        'en-IN'      | new Locale("en", "IN")
        'en-US'      | Locale.US
        'es-ES'      | new Locale("es", "ES")
        'es-MX'      | new Locale("es", "MX")
        'es-US'      | new Locale("es", "US")
        'fr-CA'      | Locale.CANADA_FRENCH
        'fr-FR'      | Locale.FRANCE
        'hi-IN'      | new Locale("hi", "IN")
        'it-IT'      | Locale.ITALY
        'ja-JP'      | Locale.JAPAN
        'pt-BR'      | new Locale("pt", "BR")
    }
}
