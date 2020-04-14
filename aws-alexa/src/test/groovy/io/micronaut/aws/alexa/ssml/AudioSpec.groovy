package io.micronaut.aws.alexa.ssml

import spock.lang.Specification

class AudioSpec extends Specification {

    void "speak example"() {
        expect:
        //tag::ssmlsample[]
        new Ssml().speak(new Ssml("Welcome to Ride Hailer. ").audio('soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01').build()).build() == '<speak>Welcome to Ride Hailer. <audio src="soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01"/></speak>'
        //end::ssmlsample[]

    }
}
