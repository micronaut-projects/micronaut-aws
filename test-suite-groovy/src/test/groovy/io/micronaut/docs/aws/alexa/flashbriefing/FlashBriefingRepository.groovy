package io.micronaut.docs.aws.alexa.flashbriefing

import groovy.transform.CompileStatic
import io.micronaut.aws.alexa.flashbriefing.FlashBriefingItem
import jakarta.inject.Singleton

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@CompileStatic
@Singleton
class FlashBriefingRepository {

    List<FlashBriefingItem> find() {
        FlashBriefingItem one = new FlashBriefingItem()
        one.uid = "EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1"
        one.updateDate = ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris"))
        one.titleText = "Multi Item JSON (TTS)"
        one.mainText = "This channel has multiple TTS JSON items. This is the first item."
        one.redirectionUrl = "https://www.amazon.com"

        FlashBriefingItem two = new FlashBriefingItem()
        two.uid = "EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2"
        two.updateDate = ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris"))
        two.titleText = "Multi Item JSON (TTS)"
        two.mainText = "This channel has multiple TTS JSON items. This is the second item."
        two.redirectionUrl = "https://www.amazon.com"
        [one, two]
    }
}
