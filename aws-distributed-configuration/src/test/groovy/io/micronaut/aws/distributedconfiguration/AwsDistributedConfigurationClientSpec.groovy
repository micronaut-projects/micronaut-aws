package io.micronaut.aws.distributedconfiguration

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.StringUtils
import io.micronaut.runtime.server.EmbeddedServer
import jakarta.inject.Singleton
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class AwsDistributedConfigurationClientSpec extends Specification {

    void "AwsDistributedConfigurationProperties values are loaded from configuration"() {
        given:
        Map<String, Object> properties = [
                'aws.distributed-configuration.secrets': 'SECRETS:path/dev;path/prod;shared/commons',
        ]
        when:
        ApplicationContext context = ApplicationContext.run(properties)
        AwsDistributedConfigurationProperties awsDistributedConfiguration = context.getBean(AwsDistributedConfigurationProperties)

        then:
        awsDistributedConfiguration.secrets == 'SECRETS:path/dev;path/prod;shared/commons'

        cleanup:
        context.close()
    }

    @RestoreSystemProperties
    void "configuration resolution precedence is no more loaded by user specified path"(String env,
                                                                                        String expected) {
        given:
        System.setProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, StringUtils.TRUE)
        Map<String, Object> properties = [
                'aws.distributed-configuration.secrets': 'SECRETS:path/dev;',
                'micronaut.config-client.enabled': true,
        ]

        when:
        EmbeddedServer embeddedServer = env ? ApplicationContext.run(EmbeddedServer, properties, env) : ApplicationContext.run(EmbeddedServer,  properties)
        ApplicationContext context = embeddedServer.applicationContext
        Optional<String> clientSecretOptional = context.getProperty('micronaut.security.oauth2.clients.companyauthserver.client-secret', String)

        then:
        clientSecretOptional.isPresent()
        clientSecretOptional.get() == expected

        cleanup:
        context.close()
        embeddedServer.close()

        where:
         env   || expected
         null  || 'applicationYYY'
    }

    @Requires(beans = [AwsDistributedConfigurationProperties, MockKeyValuesFetcher])
    @BootstrapContextCompatible
    @Singleton
    static class MockAwsDistributedConfigurationClient extends AwsDistributedConfigurationClient {

        MockAwsDistributedConfigurationClient(MockKeyValuesFetcher keyValueFetcher,
                                              AwsDistributedConfigurationProperties applicationConfiguration) {
            super(keyValueFetcher, applicationConfiguration)
        }

        @Override
        @NonNull
        protected String getPropertySourceName() {
            'mocksecretsmanager'
        }

        @Override
        String getDescription() {
            'mock secrets manager'
        }
    }

    @BootstrapContextCompatible
    @Singleton
    static class MockKeyValuesFetcher implements KeyValueFetcherByPath {
        Map<String, Map<String, String>> m = [
                'path/dev':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'applicationYYY'
                        ],
                'path/prod':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'application_fooYYY'
                        ],
                'shared/commons':
                        [
                                'micronaut.security.oauth2.clients.companyauthserver.client-secret': 'myappYYY'
                        ]
        ]

        @Override
        Optional<Map> keyValuesByPrefix(@NonNull String keyOrValue, String version) {
            String k = m.keySet().find { it.startsWith(keyOrValue) }
            (k) ? Optional.of(m[k]) : Optional.empty()
        }
    }
}
