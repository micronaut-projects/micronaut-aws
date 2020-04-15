package io.micronaut.aws.alexa.flashbriefing;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Singleton
public class FlashBriefingRepository {

    public List<FlashBriefingItem> find() {
        FlashBriefingItem one = new FlashBriefingItem();
        one.setUid("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1");
        one.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris")));
        one.setTitleText("Multi Item JSON (TTS)");
        one.setMainText("This channel has multiple TTS JSON items. This is the first item.");
        one.setRedirectionUrl("https://www.amazon.com");

        FlashBriefingItem two = new FlashBriefingItem();
        two.setUid("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2");
        two.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2016, 4, 10, 0, 0), ZoneId.of("Europe/Paris")));
        two.setTitleText("Multi Item JSON (TTS)");
        two.setMainText("This channel has multiple TTS JSON items. This is the second item.");
        two.setRedirectionUrl("https://www.amazon.com");
        return Arrays.asList(one, two);
    }
}
