package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.function.FunctionBean
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Function

class AsyncSupportSpec extends Specification {

    void 'micronaut request handler wait until all async methods are finished'() {
        given:
        ApplicationContextBuilder builder = new LambdaApplicationContextBuilder()
                .properties('spec.name': 'AsyncSupportSpec')
        Handler handler = new Handler(builder)
        ApplicationContext context = handler.applicationContext

        when:
        String output = handler.handleRequest('hello', Mock(Context))
        then:
        output == 'olleh'
        context.containsBean(AsyncWorker)

        when:
        AsyncWorker worker = context.getBean(AsyncWorker)
        then:
        worker
        worker.processed.get()

        cleanup:
        handler.close()
    }

    void 'micronaut request handler throw exception if there are unfinished async tasks'() {
        given:
        ApplicationContextBuilder builder = new LambdaApplicationContextBuilder()
                .properties(
                        'micronaut.aws.async.await-termination': '1ms',
                        'spec.name': 'AsyncSupportSpec',
                )

        Handler handler = new Handler(builder)

        when:
        handler.handleRequest('hello', Mock(Context))
        then:
        IllegalStateException e = thrown(IllegalStateException)
        e.message.contains('There are still')

        cleanup:
        handler.close()
    }

    void 'micronaut request stream handler wait until all async methods are finished'() {
        given:
        StreamHandler handler = new StreamHandler()
        ApplicationContext context = handler.applicationContext

        when:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ByteArrayInputStream bais = new ByteArrayInputStream('hello'.bytes)
        handler.handleRequest(bais, baos, Mock(Context))
        then:
        new String(baos.toByteArray()) == 'olleh'
        context.containsBean(AsyncWorker)

        when:
        AsyncWorker worker = context.getBean(AsyncWorker)
        then:
        worker
        worker.processed.get()

        cleanup:
        handler.close()
    }

    void 'micronaut stream request handler throw exception if there are unfinished async tasks'() {
        given:
        ApplicationContextBuilder builder = new LambdaApplicationContextBuilder()
                .properties(
                        'micronaut.aws.async.await-termination': '1ms',
                        'spec.name': 'AsyncSupportSpec',
                )

        StreamHandler handler = new StreamHandler(builder.build())

        when:
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        ByteArrayInputStream bais = new ByteArrayInputStream('hello'.bytes)
        handler.handleRequest(bais, baos, Mock(Context))
        then:
        IllegalStateException e = thrown(IllegalStateException)
        e.message.contains('There are still')

        cleanup:
        handler.close()
    }

    @Singleton
    @CompileStatic
    @Requires(property = 'spec.name', value = 'AsyncSupportSpec')
    static class AsyncWorker {

        final AtomicBoolean processed = new AtomicBoolean()

        @Async
        void updateProcessedAsync() {
            Thread.sleep(100)
            processed.set(true)
        }

    }

    @CompileStatic
    @InheritConstructors
    static class Handler extends MicronautRequestHandler<String, String> {

        @Inject AsyncWorker worker

        @Override
        String execute(String input) {
            worker.updateProcessedAsync()
            return input.reverse()
        }

    }

    @Requires(property = 'spec.name', value = 'AsyncSupportSpec')
    @FunctionBean('AsyncSupportSpecFun')
    static class EventLogger implements Function<String, String> {

        private final AsyncWorker worker

        EventLogger(AsyncWorker worker) {
            this.worker = worker
        }

        @Override
        String apply(String input)  {
            worker.updateProcessedAsync()
            return input.reverse()
        }

    }

    @InheritConstructors
    class StreamHandler extends MicronautRequestStreamHandler {

        @Override
        protected String resolveFunctionName(Environment env) {
            return 'AsyncSupportSpecFun'
        }

        @Override
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            return super.newApplicationContextBuilder().properties('spec.name': 'AsyncSupportSpec')
        }

    }
}
