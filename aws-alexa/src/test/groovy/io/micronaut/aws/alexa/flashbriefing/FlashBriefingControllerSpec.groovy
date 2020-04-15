package io.micronaut.aws.alexa.flashbriefing

import io.micronaut.aws.EmbeddedServerSpecification
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest

class FlashBriefingControllerSpec extends EmbeddedServerSpecification {

    void "fetch news"() {
        when:
        List<Map> news = client.retrieve(HttpRequest.GET('/news'), Argument.listOf(Map))

        then:
        news
        news.size()
        news.uid == ['EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1', 'EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2']
    }


}
