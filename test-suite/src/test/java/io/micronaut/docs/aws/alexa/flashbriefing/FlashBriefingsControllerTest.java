package io.micronaut.docs.aws.alexa.flashbriefing;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class FlashBriefingsControllerTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void fetchNews() {
        List<Map<String, Object>> news = httpClient.toBlocking().retrieve(HttpRequest.GET("/news"), Argument.listOf(Argument.mapOf(String.class, Object.class)));

        assertEquals(2, news.size());
        assertEquals(List.of("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1", "EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2"),
                news.stream().map(item -> item.get("uid")).toList());
    }
}
