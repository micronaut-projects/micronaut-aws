package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.*
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.Any
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

class LambdaContextSpec extends Specification {

    void "Lambda Context beans are registered"() {
        given:
        ApplicationContextBuilder builder = ApplicationContext.builder()
                .properties(Collections.singletonMap(
                        "spec.name", "LambdaContextSpec"
                ))
        MicronautRequestHandler handler = new LambdaContextSpecHandler(builder)

        when:
        String result = handler.handleRequest("Foo", createContext())

        then:
        handler.applicationContext.containsBean(Context)
        handler.applicationContext.containsBean(LambdaLogger)
        handler.applicationContext.containsBean(CognitoIdentity)
        handler.applicationContext.containsBean(ClientContext)
        "XXX" == result
    }

    void "verify LambdaLogger CognitoIdentity and ClientContext are not registered if null"() {
        given:
        ApplicationContextBuilder builder = ApplicationContext.builder()
                .properties(Collections.singletonMap(
                        "spec.name", "LambdaContextSpec"
                ))
        MicronautRequestHandler handler = new LambdaContextSpecHandler(builder)

        when:
        String result = handler.handleRequest("Foo", createContextWithoutCollaborators())

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
    }

    static interface RequestIdProvider {
        @NonNull
        Optional<String> requestId();
    }

    @Requires(property = "spec.name", value = "LambdaContextSpec")
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

    static class LambdaContextSpecHandler extends MicronautRequestHandler<String, String> {
        @Inject
        RequestIdProvider requestIdProvider
        LambdaContextSpecHandler(ApplicationContextBuilder builder) {
            super(builder)
        }

        @Override
        String execute(String input) {
            requestIdProvider.requestId().orElse(null)
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
