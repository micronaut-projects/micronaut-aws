package io.micronaut.docs.function.aws;

//tag::clazz[]
import io.micronaut.function.FunctionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;

@FunctionBean("eventlogger")
public class EventLogger implements Consumer<String> {
    private static final Logger LOG = LoggerFactory.getLogger(EventLogger.class);
    @Override
    public void accept(String input) {
        LOG.info("Received: {}", input);
    }
}
//end::clazz[]
