import json
from typing import Annotated

from jakarta.inject import Inject
from micronaut.http import HttpRequest
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@MicronautTest
class FlashBriefingsControllerTest:
    http_client: Annotated[HttpClient, Inject, Client("/")]

    @Test
    def test_fetch_news(self):
        news = json.loads(self.http_client.toBlocking().retrieve(HttpRequest.GET("/news")))

        assert len(news) == 2
        assert [item["uid"] for item in news] == ["EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1", "EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2"]
