package io.micronaut.aws.alexa.flashbriefing

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import io.micronaut.aws.alexa.builders.ApplicationContextSpecification
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Unroll
import javax.validation.Validator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.stream.Collectors

class FlashBriefingItemSpec extends ApplicationContextSpecification {
    private final ZoneId ZONE_EUROPE_PARIS = ZoneId.of("Europe/Paris");

    @Shared
    Validator validator = applicationContext.getBean(Validator)

    @Shared
    ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

    @PendingFeature
    void "updateDate must be at most one week old"() {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        when:
        LocalDateTime dt = LocalDate.now().minusDays(8)
        item.updateDate = ZonedDateTime.of(LocalDateTime.of(dt.year,dt.month,dt.dayOfMonth,22,34,51), ZoneOffset.UTC)

        then:
        !validator.validate(item).isEmpty()
    }

    void "items should sort newest to oldest based on updateDate"() {
        given:
        FlashBriefingItem one = new FlashBriefingItem();
        one.setTitleText("1 - CORONAVIRUS IN SPAIN: DAY 47, 157,022 CASES");
        one.setMainText("Let's go. BREAK: Spain now has 157,022 cases of Coronavirus and 15,843 dead.");
        one.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2020, 4, 10, 11, 33), ZONE_EUROPE_PARIS));
        one.setUid("1248544710588878849");
        one.setRedirectionUrl("https://twitter.com/matthewbennett/status/1248544710588878849");

        FlashBriefingItem two = new FlashBriefingItem();
        two.setTitleText("2 - CORONAVIRUS IN SPAIN: DAY 47, 157,022 CASES");
        two.setMainText("The official Coronavirus dead numbers in Spain in the last 10 days: 864 950 932 809 674 637 743 757 683 Today: 605");
        two.setUpdateDate(ZonedDateTime.of(LocalDateTime.of(2020, 4, 10, 11, 55), ZONE_EUROPE_PARIS));
        two.setUid("1248550198495842304");
        two.setRedirectionUrl("https://twitter.com/matthewbennett/status/1248550198495842304");

        String expectedMainText = "The official Coronavirus dead numbers in Spain in the last 10 days: 864 950 932 809 674 637 743 757 683 Today: 605"

        expect:
        [Arrays.asList(two, two), Arrays.asList(one, two)].each { List<FlashBriefingItem> l ->
            assert l.stream().sorted().collect(Collectors.toList()).get(0).mainText == expectedMainText
        }
    }

    void "check JSON serialization formats date as ISO 8601"() {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        when:
        String json = objectMapper.writeValueAsString(item)
        Map m = new JsonSlurper().parseText(json)
        LocalDateTime dt = yesterday()

        then:
        m.keySet() == ['uid', 'updateDate', 'titleText', 'mainText','streamUrl', 'redirectionUrl'] as Set<String>
        m["uid"] == "urn:uuid:1335c695-cfb8-4ebb-abbd-80da344efa6b"
        m["updateDate"] == "${dt.year}-${dt.monthValue < 10 ? '0' + dt.monthValue : dt.monthValue}-${dt.dayOfMonth < 10 ? '0' + dt.dayOfMonth : dt.dayOfMonth}T22:34:51+0000"
        m["titleText"] == "Amazon Developer Blog, week in review May 23rd"
        m["mainText"] == ""
        m["streamUrl"] == "https://developer.amazon.com/public/community/blog/myaudiofile.mp3"
        m["redirectionUrl"] == "https://developer.amazon.com/public/community/blog"
    }

    void "streamUrl is optional"() {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        expect:
        item.streamUrl
        validator.validate(item).isEmpty()

        when:
        item.streamUrl = null

        then:
        validator.validate(item).isEmpty()
    }

    void "mainText cannot have more than 4500 chars"() {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        expect:
        validator.validate(item).isEmpty()

        when:
        item.mainText = 'a' * 4500

        then:
        validator.validate(item).isEmpty()

        when:
        item.mainText = 'a' * 4501

        then:
        !validator.validate(item).isEmpty()
    }

    @Unroll
    void "#field is required and cannot be blank"(String field) {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        expect:
        validator.validate(item).isEmpty()

        when:
        item['uid'] = null

        then:
        !validator.validate(item).isEmpty()

        when:
        item['uid'] = ''

        then:
        !validator.validate(item).isEmpty()

        where:
        field << ['uid', 'redirectionUrl', 'titleText']
    }

    void "For audio items, mainText element is ignored, and can contain an empty string"() {
        given:
        FlashBriefingItem item = audioFlashBriefing()

        expect:
        item.mainText == ''

        and: 'no constraint violation, it is valid'
        validator.validate(item).isEmpty()

        when: 'however, mainText cannot be null'
        item.mainText = null

        then:
        !validator.validate(item).isEmpty()
    }

    private LocalDateTime yesterday() {
        LocalDateTime.now().minusDays(1)
    }

    private FlashBriefingItem audioFlashBriefing() {
        FlashBriefingItem item = new FlashBriefingItem()

        LocalDateTime dt = yesterday()

        item.with {
            uid = "urn:uuid:1335c695-cfb8-4ebb-abbd-80da344efa6b"
            updateDate = ZonedDateTime.of(LocalDateTime.of(dt.year, dt.month, dt.dayOfMonth,22,34,51), ZoneOffset.UTC)
            titleText = "Amazon Developer Blog, week in review May 23rd"
            mainText = ""
            streamUrl = "https://developer.amazon.com/public/community/blog/myaudiofile.mp3"
            redirectionUrl = "https://developer.amazon.com/public/community/blog"
        }
        item
    }
}
