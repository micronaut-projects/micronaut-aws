package io.micronaut.docs.aws.alexa.ssml;

import io.micronaut.aws.alexa.ssml.Ssml;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioSpec {

    @Test
    void speakExample() {
        assertEquals("<speak>Welcome to Ride Hailer. <audio src=\"soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01\"/></speak>",
        //tag::ssmlsample[]
        new Ssml().speak(new Ssml("Welcome to Ride Hailer. ").audio("soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01").build()).build()
        //end::ssmlsample[]
        );
    }
}
