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
    void "resolve locale from handler input"(String languageCode, String language, Locale expected) {
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
        languageCode | language          | expected
        'ar-SA'      | 'Arabic (SA)'     | new Locale("ar", "SA")
        'de-DE'      | 'German (DE)'     | Locale.GERMANY
        'en-AU'      | 'English (AU)'    | new Locale("en", "AU")
        'en-CA'      | 'English (CA)'    | Locale.CANADA
        'en-GB'      | 'English (UK)'    | Locale.UK
        'en-IN'      | 'English (IN)'    | new Locale("en", "IN")
        'en-US'      | 'English (US)'    | Locale.US
        'es-ES'      | 'Spanish (ES)'    | new Locale("es", "ES")
        'es-MX'      | 'Spanish (MX)'    | new Locale("es", "MX")
        'es-US'      | 'Spanish (US)'    | new Locale("es", "US")
        'fr-CA'      | 'French (CA)'     | Locale.CANADA_FRENCH
        'fr-FR'      | 'French (FR)'     | Locale.FRANCE
        'hi-IN'      | 'Hindi (IN)'      | new Locale("hi", "IN")
        'it-IT'      | 'Italian (IT)'    | Locale.ITALY
        'ja-JP'      | 'Japanese (JP)'   | Locale.JAPAN
        'pt-BR'      | 'Portuguese (BR)' | new Locale("pt", "BR")
    }
}
