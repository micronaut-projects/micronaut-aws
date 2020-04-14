package io.micronaut.aws.alexa.ssml

import spock.lang.Specification

class SsmlSpec extends Specification {

    void "amazon:domain with news"() {
        expect:
        '<amazon:domain name="news">A miniature manuscript written by the teenage Charlotte Bronte is returning to her childhood home in West Yorkshire after it was bought by a British museum at auction in Paris.</amazon:domain>' == new Ssml().domain(AmazonDomain.NEWS, 'A miniature manuscript written by the teenage Charlotte Bronte is returning to her childhood home in West Yorkshire after it was bought by a British museum at auction in Paris.').build()
    }

    void "amazon:domain with music"() {
        expect:
        '<amazon:domain name="music">Sweet Child O\' Mine by Guns N\' Roses became one of their most successful singles topping the billboard Hot 100 in 1988. Slash\'s guitar solo on this song was ranked the 37th greatest solo of all time. Here\'s Sweet Child O\' Mine.</amazon:domain>' == new Ssml().domain(AmazonDomain.MUSIC, 'Sweet Child O\' Mine by Guns N\' Roses became one of their most successful singles topping the billboard Hot 100 in 1988. Slash\'s guitar solo on this song was ranked the 37th greatest solo of all time. Here\'s Sweet Child O\' Mine.').build()
    }

    void "sentence"() {
        expect:
        '<s>This is a sentence</s>' == new Ssml().s('This is a sentence').build()
    }

    void "w build"() {
        expect:
        '<w role="amazon:VB">read</w>' == new Ssml().w('read', WordRole.VB).build()
    }

    void "sub"() {
        expect:
        '<sub alias="magnesium">Mg</sub>' == new Ssml().sub('Mg', 'magnesium').build()
    }

    void "voice"() {
        expect:
        '<voice name="Kendra">I am not a real human.</voice>' == new Ssml().voice('I am not a real human.', Voice.ENGLISH_AMERICAN_KENDRA).build()

        and:
        '<voice name="Brian"><lang xml:lang="en-GB">Your secret is safe with me!</lang></voice>' == new Ssml().voice(new Ssml().lang("Your secret is safe with me!", SupportedLang.EN_GB).build(), Voice.ENGLISH_BRITISH_BRIAN).build()
    }

    void "speak"() {
        expect:
        '<speak>This is what Alexa sounds like without any SSML</speak>' == new Ssml().speak('This is what Alexa sounds like without any SSML').build()
    }

    void "prosody volume"() {
        expect:
        '<prosody volume="x-loud">but also with a much higher pitch</prosody>' == new Ssml().prosody('but also with a much higher pitch', null, null, ProsodyVolume.XLOUD).build()
    }

    void "prosody pitch"() {
        expect:
        '<prosody pitch="x-high">but also with a much higher pitch</prosody>' == new Ssml().prosody('but also with a much higher pitch', null, ProsodyPitch.XHIGH, null).build()
    }

    void "paragraphs"() {
        expect:
        '<p>This is the first paragraph. There should be a pause after this text is spoken.</p><p>This is the second paragraph.</p>' == new Ssml().p('This is the first paragraph. There should be a pause after this text is spoken.').p('This is the second paragraph.').build()
    }

    void "lang with french"() {
        expect:
        'In Paris, they pronounce it <lang xml:lang="fr-FR">Paris</lang>' == new Ssml().text('In Paris, they pronounce it ').lang('Paris', SupportedLang.FR).build()
    }

    void "build with emotion"() {
        given:
        String expected = '<amazon:emotion name="excited" intensity="medium">Christina wins this round!</amazon:emotion>'

        when:
        String result = new Ssml().emotion(AmazonEmotion.EXCITED, AmazonEmotionIntensity.MEDIUM, 'Christina wins this round!').build()

        then:
        result == expected
    }

    void "break with seconds"() {
        given:
        String expected = 'There is a three second pause here <break time="3s"/> then the speech continues.'

        when:
        String result = new Ssml().text('There is a three second pause here ').breakWithSeconds(3).text(" then the speech continues.").build();

        then:
        result == expected
    }

    void "strong emphases"() {
        given:
        String expected = 'I already told you I <emphasis level="strong">really like</emphasis> that person.'

        when:
        String result = new Ssml().text('I already told you I ').emphasis('really like', EmphasisLevel.STRONG).text(" that person.").build()

        then:
        result == expected
    }
}
