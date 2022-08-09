package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class AfterExecutionEventSpec extends Specification {

    @InheritConstructors
    class Handler extends MicronautRequestHandler<String, String> {

        @Override
        String execute(String input) {
            if (input == 'foo') {
                throw new IllegalArgumentException('No foo allowed!')
            }
            return input.reverse()
        }

    }

    @InheritConstructors
    class StreamHandler extends MicronautRequestStreamHandler {

        void execute(InputStream input, OutputStream output, Context context) throws IOException {
            String inputText = input.text

            if (inputText == 'foo') {
                throw new IOException('No foo allowed!')
            }

            output.write(inputText.reverse().bytes)
            output.flush()
        }

    }

   @Inject ApplicationContext context

    void 'micronaut request handler with success'() {
        given:
        Handler handler = new Handler(context)
        when:
        String output = handler.handleRequest('hello', Mock(Context))
        then:
        output == 'olleh'

        context.getBean(AfterExecutionEventListener).lastEvent
        context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.output == output
    }

    void 'micronaut request handler with failure'() {
        given:
        Handler handler = new Handler(context)
        when:
        handler.handleRequest('foo', Mock(Context))
        then:
        thrown(IllegalArgumentException)

        context.getBean(AfterExecutionEventListener).lastEvent
        !context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.exception instanceof IllegalArgumentException
    }

    void 'micronaut steam request handler with success'() {
        given:
        StreamHandler handler = new StreamHandler(context)
        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        handler.handleRequest(new ByteArrayInputStream('hello'.bytes), outputStream, Mock(Context))
        then:
        outputStream.toString() == 'olleh'

        context.getBean(AfterExecutionEventListener).lastEvent
        context.getBean(AfterExecutionEventListener).lastEvent.success
        // no output capturing for streaming implementation
        context.getBean(AfterExecutionEventListener).lastEvent.output == null
    }

    void 'micronaut steam request handler with failure'() {
        given:
        StreamHandler handler = new StreamHandler(context)
        when:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        handler.handleRequest(new ByteArrayInputStream('foo'.bytes), outputStream, Mock(Context))
        then:
        thrown(IOException)

        context.getBean(AfterExecutionEventListener).lastEvent
        !context.getBean(AfterExecutionEventListener).lastEvent.success
        context.getBean(AfterExecutionEventListener).lastEvent.exception instanceof IOException
    }

}
