package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.*
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.Any
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.StringUtils
import io.micronaut.function.FunctionBean
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

import java.util.function.Function

class MicronautStreamHandlerLambdaContextSpec extends Specification {

    void "Lambda Context beans are registered"() {
        given:
        MicronautRequestStreamHandler handler = new LambdaContextSpecHandler()
        handler.applicationContext.start()

        when:
        ByteArrayInputStream input = new ByteArrayInputStream("Foo".bytes)
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.handleRequest(input, output, createContext())
        String result = output.toString()

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == result

        cleanup:
        handler.applicationContext.close()
    }

    void "verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        given:

        MicronautRequestStreamHandler handler = new LambdaContextSpecHandler()
        handler.applicationContext.start()

        when:
        ByteArrayInputStream input = new ByteArrayInputStream("Foo".bytes)
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.handleRequest(input, output, createContextWithoutCollaborators())
        String result = output.toString()

        then:
        handler.applicationContext.containsBean(Context)
        and: 'LambdaLogger is not registered if Lambda Context::getLambdaLogger is null'
        !handler.applicationContext.containsBean(LambdaLogger)

        and: 'CognitoIdentity is not registered if Lambda Context::getIdentity is null"'
        !handler.applicationContext.containsBean(CognitoIdentity)

        and: 'ClientContext is not registered if Lambda Context::getClientContext is null"'
        !handler.applicationContext.containsBean(ClientContext)

        and:
        "XXX" == result

        cleanup:
        handler.applicationContext.close()
    }

    static interface RequestIdProvider {
        @NonNull
        Optional<String> requestId();
    }

    @Requires(property = "spec.name", value = "MicronautStreamHandlerLambdaContextSpec")
    @Singleton
    static class DefaultRequestIdProvider implements RequestIdProvider {
        private final BeanProvider<Context> context

        DefaultRequestIdProvider(@Any BeanProvider<Context> context) {
            this.context = context
        }

        @Override
        @NonNull
        Optional<String> requestId() {
            context.isPresent() ? Optional.of(context.get().awsRequestId) : Optional.empty()
        }
    }

    static class LambdaContextSpecHandler extends MicronautRequestStreamHandler {
        @Inject
        RequestIdProvider requestIdProvider

        @Override
        @NonNull
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            super.newApplicationContextBuilder().properties(Collections.singletonMap(
                    "spec.name", "MicronautStreamHandlerLambdaContextSpec"
            ))
        }

        @Override
        protected String resolveFunctionName(Environment env) {
            "contextrequestid"
        }
    }

    @Requires(property = "spec.name", value = "MicronautStreamHandlerLambdaContextSpec")
    @FunctionBean("contextrequestid")
    static class ContextRequestIdFunction implements Function<String, String> {
        private final RequestIdProvider requestIdProvider;
        ContextRequestIdFunction(RequestIdProvider requestIdProvider) {
            this.requestIdProvider = requestIdProvider
        }

        @Override
        String apply(String s) {
            requestIdProvider.requestId().orElse(s)
        }
    }

    Context createContextWithoutCollaborators() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> null
            getClientContext() >> null
            getClientContext() >> null
            getLogger() >> null
        }
    }

    Context createContext() {
        Stub(Context) {
            getAwsRequestId() >> 'XXX'
            getIdentity() >> Mock(CognitoIdentity)
            getClientContext() >> Mock(ClientContext)
            getClientContext() >> Mock(ClientContext)
            getLogger() >> Mock(LambdaLogger)
        }
    }
}
