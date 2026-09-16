from jakarta.inject import Singleton
from java.time import LocalDateTime, ZoneId, ZonedDateTime
from micronaut.aws.alexa.flashbriefing import FlashBriefingItem


@Singleton
class FlashBriefingRepository:

    def find(self) -> list[FlashBriefingItem]:
        one = FlashBriefingItem()
        one.setUid("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1")
        one.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris")))
        one.setTitleText("Multi Item JSON (TTS)")
        one.setMainText("This channel has multiple TTS JSON items. This is the first item.")
        one.setRedirectionUrl("https://www.amazon.com")

        two = FlashBriefingItem()
        two.setUid("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2")
        two.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris")))
        two.setTitleText("Multi Item JSON (TTS)")
        two.setMainText("This channel has multiple TTS JSON items. This is the second item.")
        two.setRedirectionUrl("https://www.amazon.com")
        return [one, two]
