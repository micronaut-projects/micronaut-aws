package io.micronaut.docs.function.aws.test

//tag::clazz[]
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautLambdaTest
class RequestHandlerTest {
    @Inject
    lateinit var context: ApplicationContext

    @Test
    fun testHandler() {
        val sampleRequestHandler = SampleRequestHandler(context)
        assertEquals("Hello world", sampleRequestHandler.execute("world"))
    }
}
//end::clazz[]
