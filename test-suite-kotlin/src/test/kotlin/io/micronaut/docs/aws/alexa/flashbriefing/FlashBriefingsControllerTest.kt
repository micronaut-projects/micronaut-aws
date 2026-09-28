package io.micronaut.docs.aws.alexa.flashbriefing

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
class FlashBriefingsControllerTest {

    @Inject
    @field:Client("/")
    lateinit var httpClient: HttpClient

    @Test
    fun fetchNews() {
        val news = httpClient.toBlocking().retrieve(HttpRequest.GET<Any>("/news"), Argument.listOf(Argument.mapOf(String::class.java, Any::class.java)))

        assertEquals(2, news.size)
        assertEquals(listOf("EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_1", "EXAMPLE_CHANNEL_MULTI_ITEM_JSON_TTS_2"), news.map { it["uid"] })
    }
}
