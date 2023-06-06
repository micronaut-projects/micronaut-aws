package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.core.annotation.NonNull
import io.micronaut.function.FunctionBean
import io.micronaut.function.aws.event.AfterExecutionEvent
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import java.util.function.Function

class AfterExecutionEventSpec extends Specification {
    void 'micronaut request handler with success without AfterExecutionEvent listener'() {
        given:
        Handler handler = new Handler()
        ApplicationContext context = handler.applicationContext

        when:
        String output = handler.handleRequest('hello', Mock(Context))
        then:
        output == 'olleh'
        !context.containsBean(AfterExecutionEventListener)

        cleanup:
        handler.close()
    }

    void 'micronaut request handler with success'() {
        given:
        Handler handler = new Handler(builderWithSpecName("AfterExecutionEventSpec"))
        ApplicationContext context = handler.applicationContext

        when:
        String output = handler.handleRequest('hello', Mock(Context))
        then:
        output == 'olleh'
        context.containsBean(AfterExecutionEventListener)
        context.getBean(AfterExecutionEventListener).lastEvent
        context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.output == output

        cleanup:
        handler.close()
    }

    void 'micronaut request handler with failure'() {
        given:
        Handler handler = new Handler(builderWithSpecName("AfterExecutionEventSpec"))
        ApplicationContext context = handler.applicationContext

        when:
        handler.handleRequest('foo', Mock(Context))
        then:
        thrown(IllegalArgumentException)

        context.containsBean(AfterExecutionEventListener)
        context.getBean(AfterExecutionEventListener).lastEvent
        !context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.exception instanceof IllegalArgumentException

        cleanup:
        handler.close()
    }

    void 'micronaut steam request handler with success'() {
        given:
        StreamHandler handler = new StreamHandler()
        ApplicationContext context = handler.applicationContext

        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        handler.handleRequest(new ByteArrayInputStream('hello'.bytes), outputStream, Mock(Context))
        then:
        outputStream.toString() == 'olleh'
        context.containsBean(AfterExecutionEventListener)
        context.getBean(AfterExecutionEventListener).lastEvent
        context.getBean(AfterExecutionEventListener).lastEvent.success
        // no output capturing for streaming implementation
        context.getBean(AfterExecutionEventListener).lastEvent.output == null

        cleanup:
        handler.close()
    }

    void 'micronaut steam request handler with failure'() {
        given:
        StreamHandler handler = new StreamHandler()
        ApplicationContext context = handler.applicationContext

        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        handler.handleRequest(new ByteArrayInputStream('foo'.bytes), outputStream, Mock(Context))

        then:
        thrown(IllegalArgumentException)
        context.containsBean(AfterExecutionEventListener)
        context.getBean(AfterExecutionEventListener).lastEvent
        !context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.exception instanceof IllegalArgumentException

        cleanup:
        handler.close()
    }

    @NonNull
    private static ApplicationContextBuilder builderWithSpecName(@NonNull String specName) {
        Map<String, String> properties = Collections.singletonMap("spec.name", specName)
        ApplicationContextBuilder contextBuilder = new LambdaApplicationContextBuilder()
        contextBuilder.properties(properties)
        contextBuilder
    }

    @Requires(property = "spec.name", value = "AfterExecutionEventSpec")
    @Singleton
    @CompileStatic
    static class AfterExecutionEventListener implements ApplicationEventListener<AfterExecutionEvent> {

        AfterExecutionEvent lastEvent

        @Override
        void onApplicationEvent(AfterExecutionEvent event) {
            lastEvent = event
        }

    }

    @Requires(property = "spec.name", value = "AfterExecutionEventSpec")
    @FunctionBean("afterExecutionFun")
    static class EventLogger implements Function<String, String> {
        private static final Logger LOG = LoggerFactory.getLogger(EventLogger.class);
        @Override
        String apply(String input)  {
            testScenario().apply(input)
        }
    }

    @InheritConstructors
    class Handler extends MicronautRequestHandler<String, String> {
        @Override
        String execute(String input) {
            testScenario().apply(input)
        }
    }

    @InheritConstructors
    class StreamHandler extends MicronautRequestStreamHandler {

        @Override
        protected String resolveFunctionName(Environment env) {
            return "afterExecutionFun"
        }

        @Override
        @NonNull
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            return super.newApplicationContextBuilder().properties(Collections.singletonMap("spec.name", "AfterExecutionEventSpec"))
        }
    }

    private static Function<String, String> testScenario() {
        return new Function<String, String>() {
            @Override
            String apply(String input) {
                if (input == 'foo') {
                    throw new IllegalArgumentException('No foo allowed!')
                }
                return input.reverse()
            }
        }
    }
}
