# tag::clazz[]
from functools import cmp_to_key

from jakarta.validation import Validator
from micronaut.aws.alexa.flashbriefing import FlashBriefingItem
from micronaut.http.annotation import Controller, Get

from .FlashBriefingRepository import FlashBriefingRepository


@Controller("/news")
class FlashBriefingsController:

    def __init__(self, validator: Validator, flash_briefing_repository: FlashBriefingRepository):
        self.validator = validator
        self.flash_briefing_repository = flash_briefing_repository

    @Get  # <1>
    def index(self) -> list[FlashBriefingItem]:
        items = [item for item in self.flash_briefing_repository.find()
                 if self.validator.validate(item).isEmpty()]  # <2>
        items.sort(key=cmp_to_key(lambda a, b: a.compareTo(b)))  # <3>
        return items[:5]  # <4>
# end::clazz[]
