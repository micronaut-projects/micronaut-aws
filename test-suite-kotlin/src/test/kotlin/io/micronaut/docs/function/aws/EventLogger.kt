package com.example
//tag::clazz[]
import io.micronaut.function.FunctionBean
import org.slf4j.LoggerFactory
import java.util.function.Consumer

@FunctionBean("eventlogger")
class EventLogger : Consumer<String> {
    override fun accept(input: String) {
        LOG.info("Received: {}", input)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(EventLogger::class.java)
    }
}
//end::clazz[]