package io.micronaut.docs.function.aws

//tag::clazz[]
import groovy.transform.CompileStatic
import io.micronaut.function.FunctionBean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

@CompileStatic
@FunctionBean('eventlogger')
class EventLogger implements Consumer<String> {
    private static final Logger LOG = LoggerFactory.getLogger(EventLogger.class)
    @Override
    void accept(String input) {
        LOG.info("Received: {}", input)
    }
}
//end::clazz[]
