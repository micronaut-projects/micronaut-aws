from micronaut.aws.alexa.ssml import Ssml
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest(startApplication=False)
class AudioSpec:

    @Test
    def test_speak_example(self):
        assert (
            # tag::ssmlsample[]
            Ssml().speak(Ssml("Welcome to Ride Hailer. ").audio("soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01").build()).build()
            # end::ssmlsample[]
            == '<speak>Welcome to Ride Hailer. <audio src="soundbank://soundlibrary/transportation/amzn_sfx_car_accelerate_01"/></speak>'
        )
