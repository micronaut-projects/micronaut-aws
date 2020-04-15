package io.micronaut.aws.alexa.flashbriefing;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/news")
public class FlashBriefingsController {

    private final Validator validator;
    private final FlashBriefingRepository flashBriefingRepository;

    public FlashBriefingsController(Validator validator,
                                    FlashBriefingRepository flashBriefingRepository) {
        this.validator = validator;
        this.flashBriefingRepository = flashBriefingRepository;
    }

    @Get // <1>
    public List<FlashBriefingItem> index() {
        return flashBriefingRepository.find()
                .stream()
                .filter(item -> validator.validate(item).isEmpty()) // <2>
                .sorted() // <3>
                .limit(5) // <4>
                .collect(Collectors.toList());
    }

}
