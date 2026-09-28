package io.micronaut.docs.aws.alexa.flashbriefing

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class FlashBriefingsControllerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient httpClient

    void "fetch news"() {
        when:
        List<Map> news = httpClient.toBlocking().retrieve(HttpRequest.GET('/news'), Argument.listOf(Map))

        then:
        news.size() == 2
        news.uid == ['EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1', 'EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2']
    }
}
