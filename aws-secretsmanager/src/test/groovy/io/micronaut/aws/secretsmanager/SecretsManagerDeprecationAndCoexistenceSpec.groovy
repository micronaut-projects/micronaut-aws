package io.micronaut.aws.secretsmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class SecretsManagerDeprecationAndCoexistenceSpec extends Specification {

    void "legacy secrets manager still resolves with configured grouped prefixes"() {
        given:
        ApplicationContext legacyContext = ApplicationContext.run([
            'spec.name': 'SecretsManagerDeprecationAndCoexistenceSpec',
            'micronaut.application.name': 'myapp',
            'aws.secretsmanager.enabled': true,
            'aws.secretsmanager.secrets': [
                ['secret-name': 'oauthcompanyauthserver', 'prefix': 'datasources.default']
            ]
        ], Environment.TEST)
        SecretsManagerConfigurationClient legacyClient = legacyContext.getBean(SecretsManagerConfigurationClient)

        when:
        def propertySource = io.micronaut.aws.secretsmanager.SecretsManagerDeprecationAndCoexistenceSpec.first(legacyClient.getPropertySources(legacyContext.environment))

        then:
        propertySource.name == 'awssecretsmanager'
        propertySource.get('datasources.default.micronaut.security.oauth2.clients.companyauthserver.client-id') == 'XXX'
        propertySource.get('datasources.default.micronaut.security.oauth2.clients.companyauthserver.client-secret') == 'YYY'

        cleanup:
        legacyContext.close()
    }

    private static <T> T first(org.reactivestreams.Publisher<T> publisher) {
        def holder = new java.util.concurrent.atomic.AtomicReference<T>()
        def error = new java.util.concurrent.atomic.AtomicReference<Throwable>()
        def latch = new java.util.concurrent.CountDownLatch(1)
        publisher.subscribe(new org.reactivestreams.Subscriber<T>() {
            @Override
            void onSubscribe(org.reactivestreams.Subscription subscription) {
                subscription.request(Long.MAX_VALUE)
            }

            @Override
            void onNext(T t) {
                holder.set(t)
            }

            @Override
            void onError(Throwable t) {
                error.set(t)
                latch.countDown()
            }

            @Override
            void onComplete() {
                latch.countDown()
            }
        })
        assert latch.await(5, TimeUnit.SECONDS): 'Timed out waiting for secrets manager property sources'
        if (error.get() != null) {
            throw error.get()
        }
        return holder.get()
    }

    @io.micronaut.context.annotation.Requires(property = 'spec.name', value = 'SecretsManagerDeprecationAndCoexistenceSpec')
    @io.micronaut.context.annotation.Primary
    @jakarta.inject.Singleton
    static class MockSecretsClient extends SecretsManagerPropertySourceImporterSpec.MockSecretsClient {
        @Override
        software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse listSecrets(software.amazon.awssdk.services.secretsmanager.model.ListSecretsRequest listSecretsRequest) {
            if (listSecretsRequest.nextToken() == null && listSecretsRequest.filters().first().values().contains('/config/myapp/')) {
                return software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse.builder()
                    .secretList(software.amazon.awssdk.services.secretsmanager.model.SecretListEntry.builder().name('/config/myapp/oauthcompanyauthserver').build())
                    .build()
            }
            return software.amazon.awssdk.services.secretsmanager.model.ListSecretsResponse.builder().secretList([]).build()
        }

        @Override
        software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse getSecretValue(software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest getSecretValueRequest) {
            if (getSecretValueRequest.secretId() == '/config/myapp/oauthcompanyauthserver') {
                return software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse.builder()
                    .secretString('{"micronaut.security.oauth2.clients.companyauthserver.client-id":"XXX","micronaut.security.oauth2.clients.companyauthserver.client-secret":"YYY"}')
                    .build()
            }
            return super.getSecretValue(getSecretValueRequest)
        }
    }
}
