package io.micronaut.docs.aws.alexa.ssml

import io.micronaut.aws.alexa.ssml.Ssml
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AudioSpec {

    @Test
    fun speakExample() {
        assertEquals("<speak>Welcome to Ride Hailer. <audio src=\"soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01\"/></speak>",
        //tag::ssmlsample[]
        Ssml().speak(Ssml("Welcome to Ride Hailer. ").audio("soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01").build()).build()
        //end::ssmlsample[]
        )
    }
}
