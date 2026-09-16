package io.micronaut.docs.aws.alexa.flashbriefing

//tag::clazz[]
import io.micronaut.aws.alexa.flashbriefing.FlashBriefingItem
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.validation.Validator

@Controller("/news")
class FlashBriefingsController(
    private val validator: Validator,
    private val flashBriefingRepository: FlashBriefingRepository
) {

    @Get // <1>
    fun index(): List<FlashBriefingItem> {
        return flashBriefingRepository.find()
            .filter { item -> validator.validate(item).isEmpty() } // <2>
            .sorted() // <3>
            .take(5) // <4>
    }

}
//end::clazz[]
