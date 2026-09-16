package io.micronaut.docs.aws.alexa.flashbriefing

//tag::clazz[]
import groovy.transform.CompileStatic
import io.micronaut.aws.alexa.flashbriefing.FlashBriefingItem
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.validation.Validator

@CompileStatic
@Controller("/news")
class FlashBriefingsController {

    private final Validator validator
    private final FlashBriefingRepository flashBriefingRepository

    FlashBriefingsController(Validator validator,
                             FlashBriefingRepository flashBriefingRepository) {
        this.validator = validator
        this.flashBriefingRepository = flashBriefingRepository
    }

    @Get // <1>
    List<FlashBriefingItem> index() {
        flashBriefingRepository.find()
                .findAll { FlashBriefingItem item -> validator.validate(item).isEmpty() } // <2>
                .sort() // <3>
                .take(5) // <4>
    }

}
//end::clazz[]
